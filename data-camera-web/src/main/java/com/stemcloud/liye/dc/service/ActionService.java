package com.stemcloud.liye.dc.service;

import com.google.gson.Gson;
import com.stemcloud.liye.dc.common.ExpStatus;
import com.stemcloud.liye.dc.common.GV;
import com.stemcloud.liye.dc.common.SensorType;
import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.data.RecorderDevices;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.message.SensorStatus;
import com.stemcloud.liye.dc.util.ExecutorUtil;
import com.stemcloud.liye.dc.util.LiveRecorderUtil;
import com.stemcloud.liye.dc.util.RedisKeyUtils;
import com.stemcloud.liye.dc.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  实验的监控、录制操作
 * @author liye on 2018/1/11
 */
@Service
public class ActionService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExperimentRepository experimentRepository;
    private final RecorderRepository recorderRepository;
    private final SensorRepository sensorRepository;
    private final AppRepository appRepository;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    LiveRecorderUtil recorderUtil;

    public ActionService(ExperimentRepository experimentRepository, RecorderRepository recorderRepository, SensorRepository sensorRepository, AppRepository appRepository) {
        this.experimentRepository = experimentRepository;
        this.recorderRepository = recorderRepository;
        this.sensorRepository = sensorRepository;
        this.appRepository = appRepository;
    }

    /**
     * 返回从服务器获取的当前实验状态
     * @param expId 实验ID
     * @return
     * NOT_BOUND_SENSOR, MONITORING_NOT_RECORDING, MONITORING_AND_RECORDING, NOT_MONITOR, UNKNOWN
     */
    public ExpStatus expCurrentStatus(long expId){
        ExperimentInfo exp = experimentRepository.findOne(expId);
        Boolean hasSensor = false;
        for (TrackInfo track: exp.getTrackInfoList()){
            if (track.getIsDeleted() == 0 && track.getSensor() != null){
                hasSensor = true;
                break;
            }
        }
        if (!hasSensor){
            return ExpStatus.NOT_BOUND_SENSOR;
        } else {
            if (exp.getIsMonitor() == 1 && exp.getIsRecorder() == 0) {
                return ExpStatus.MONITORING_NOT_RECORDING;
            } else if (exp.getIsMonitor() == 1 && exp.getIsRecorder() == 1) {
                return ExpStatus.MONITORING_AND_RECORDING;
            } else if (exp.getIsMonitor() == 0 && exp.getIsRecorder() == 0){
                return ExpStatus.NOT_MONITOR;
            }
        }
        return ExpStatus.UNKNOWN;
    }

    /**
     * 获取当前场景的全局实验状态
     * @param appId
     * @return 6种状态
     */
    @Transactional(rollbackFor = Exception.class)
    public ExpStatus expAllStatus(long appId){
        List<ExperimentInfo> experiments = experimentRepository.findByAppAndIsDeletedOrderByCreateTime(appRepository.findOne(appId), 0);
        List<Long> notInMonitorIds = new ArrayList<Long>();
        List<Long> notInRecordIds = new ArrayList<Long>();
        int sensorExp = 0;
        // 选出当前绑定了设备，但是又没有处于监控状态的实验
        for (ExperimentInfo exp: experiments){
            Boolean hasSensor = false;
            for (TrackInfo track : exp.getTrackInfoList()){
                if (track.getSensor() != null){
                    hasSensor = true;
                    break;
                }
            }
            if (hasSensor){
                sensorExp ++;
                if (exp.getIsMonitor() == 0){
                    notInMonitorIds.add(exp.getId());
                }
                if (exp.getIsRecorder() == 0){
                    notInRecordIds.add(exp.getId());
                }
            }
        }
        if (sensorExp == 0){
            return ExpStatus.NO_AVAILABLE_SENSOR;
        }

        if (notInMonitorIds.isEmpty() && notInRecordIds.isEmpty()){
            return ExpStatus.ALL_MONITORING_AND_ALL_RECORDING;
        } else if (notInMonitorIds.isEmpty() && sensorExp > notInRecordIds.size()){
            return ExpStatus.ALL_MONITORING_AND_PART_RECORDING;
        } else if (notInMonitorIds.isEmpty() && sensorExp == notInRecordIds.size()){
            return ExpStatus.ALL_MONITORING_AND_NO_RECORDING;
        } else if (!notInMonitorIds.isEmpty() && notInRecordIds.isEmpty()) {
            return ExpStatus.UNKNOWN;
        } else if (!notInMonitorIds.isEmpty()){
            return ExpStatus.HAS_NOT_MONITOR;
        }

        return ExpStatus.UNKNOWN;
    }

    /**
     * 开始监控
     * @param expId 实验ID
     * @return response
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startMonitor(long expId) {
        Map<String, Object> response = new HashMap<String, Object>();

        experimentRepository.monitorExp(expId, 1);
        response.put("sensor", getSensorIdsOfExp(expId));

        ExecutorUtil.REDIS_EXECUTOR.submit(new SyncSendRedisMessage(expId, GV.MONITOR, 1));
        logger.info("Start monitor experiment {}", expId);

        return response;
    }

    /**
     * 结束监控
     * @param expId 实验ID
     * @param isSave 结束监控时判断是否保存正在录制的片段，1 -> 保存，2 -> 不保存
     * @param name 片段名
     * @param desc 片段描述
     * @return response
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> endMonitor(long expId, int isSave, String name, String desc) {
        Map<String, Object> response = new HashMap<String, Object>();

        experimentRepository.monitorAndRecorderExp(expId, 0, 0);
        response.put("sensor", getSensorIdsOfExp(expId));

        // 若还在录制状态，则结束录制
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        if (experiment.getIsRecorder() == 1) {
            Map<String, Object> r = endRecord(expId, isSave, name, desc);
            response.put("recorder", r.get("recorder"));
        }

        ExecutorUtil.REDIS_EXECUTOR.submit(new SyncSendRedisMessage(expId, GV.MONITOR, 0));
        logger.info("End monitor experiment {}, isSave = {}", expId, isSave);

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Map> allMonitor(long appId, int action, int isSave, String name, String desc) {
        Map<Long, Map> result = new HashMap<Long, Map>();

        List<ExperimentInfo> experiments = experimentRepository.findByAppAndIsDeletedOrderByCreateTime(appRepository.findOne(appId), 0);
        for (ExperimentInfo exp: experiments){
            Boolean hasSensor = false;
            for (TrackInfo track : exp.getTrackInfoList()){
                if (track.getSensor() != null){
                    hasSensor = true;
                    break;
                }
            }
            if (hasSensor){
                boolean noChange = (action == 1 && exp.getIsMonitor() == 1) || (action == 0 && exp.getIsMonitor() == 0);
                if (!noChange){
                    if (action == 1) {
                        result.put(exp.getId(), startMonitor(exp.getId()));
                    } else if (action == 0) {
                        result.put(exp.getId(), endMonitor(exp.getId(), isSave, name, desc));
                    }
                    logger.info("--> Global change experiment monitor state, action={}, isSave={}, expId={}", action, isSave, exp.getId());
                }
            }
        }

        return result;
    }

    /** 获取当前实验的设备ID **/
    private List<Long> getSensorIdsOfExp(long expId) {
        List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);
        List<Long> ids = new ArrayList<Long>();
        for (SensorInfo sensor : sensors) {
            ids.add(sensor.getId());
        }
        return ids;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startRecord(long expId) {
        Map<String, Object> response = new HashMap<String, Object>();

        // -- 检测实验是否已开始
        RecorderInfo checkR = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
        if (checkR != null) {
            logger.warn("Start recorder, but recorder {} has already started", checkR.getId());
            response.put("message", "recorder started");
            return response;
        }

        // -- 新建片段记录
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        List<RecorderDevices> devices = new ArrayList<RecorderDevices>();
        for (TrackInfo track : experiment.getTrackInfoList()) {
            SensorInfo sensor = track.getSensor();
            List<String> legend = Arrays.asList(sensor.getSensorConfig().getDimension().split(GV.SEP1));
            devices.add(new RecorderDevices(sensor.getId(), sensor.getTrackId(), legend));
            // 摄像头，开始录制
            if (track.getType() == SensorType.VIDEO.getValue()) {
                // recorderUtil.startRecordByFrame(sensor.getMark(), 0, sensor.getAppId(), experiment.getId(), sensor.getId());
            }
        }
        experimentRepository.recorderExp(expId, 1);
        RecorderInfo recorderInfo = new RecorderInfo();
        recorderInfo.setExpId(expId);
        recorderInfo.setAppId(experiment.getApp().getId());
        recorderInfo.setIsRecorder(1);
        recorderInfo.setStartTime(new Date());
        recorderInfo.setDevices(new Gson().toJson(devices));
        recorderInfo.setName(experiment.getName());
        recorderInfo.setDescription(experiment.getName());
        recorderRepository.save(recorderInfo);

        // -- send message
        ExecutorUtil.REDIS_EXECUTOR.submit(new SyncSendRedisMessage(expId, GV.RECORD, 1));

        response.put("sensor", getSensorIdsOfExp(expId));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> endRecord(long expId, int isSave, String name, String desc) {
        Map<String, Object> response = new HashMap<String, Object>();

        RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
        if (recorderInfo == null){
            logger.warn("End record, recorder is null or has already ended");
            response.put("message", "recorder ended");
            return response;
        }

        // -- 更改experiment表的状态
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        experimentRepository.recorderExp(expId, 0);

        // -- 遍历实验轨迹，若有摄像头，则保存录制的视频片段
        Date endTime = new Date();
        for (TrackInfo track : experiment.getTrackInfoList()) {
            if (track.getSensor() != null && track.getType() == SensorType.VIDEO.getValue()) {
                recorderUtil.endRecorderByFrame(recorderInfo, track.getSensor().getId(), track.getId(), isSave);
            }
        }

        // -- 保存recorder记录
        String dataName = (name == null || name.isEmpty())?"实验{" + experiment.getName() + "}的片段":name;
        recorderRepository.endRecorder(recorderInfo.getId(), endTime, Math.abs(isSave - 1), dataName, desc);
        response.put("recorder", recorderInfo.getId());
        response.put("sensor", getSensorIdsOfExp(expId));

        // -- send message
        ExecutorUtil.REDIS_EXECUTOR.submit(new SyncSendRedisMessage(expId, GV.RECORD, 0));

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Map> allRecorder(long appId, int action, int isSave, String name, String desc) {
        Map<Long, Map> result = new HashMap<Long, Map>();

        List<ExperimentInfo> experiments = experimentRepository.findByAppAndIsDeletedOrderByCreateTime(appRepository.findOne(appId), 0);
        for (ExperimentInfo exp: experiments){
            Boolean hasSensor = false;
            for (TrackInfo track : exp.getTrackInfoList()){
                if (track.getSensor() != null){
                    hasSensor = true;
                    break;
                }
            }
            if (hasSensor && exp.getIsMonitor() == 1){
                if (!(action == 0 && exp.getIsRecorder() == 0)){
                    // -- 开始录制时，还有之前的记录在，需判定是否保存之前的记录
                    if (action == 1 && exp.getIsRecorder() == 1){
                        endRecord(exp.getId(), isSave, "", "");
                        result.put(exp.getId(), startRecord(exp.getId()));
                    } else {
                        result.put(exp.getId(), endRecord(exp.getId(), isSave, name, desc));
                    }
                    logger.info("--> Global change experiment record state, action={}, isSave={}, expId={}", action, isSave, exp.getId());
                }
            }
        }

        return result;
    }

    /**
     * 将传感器组的监控/录制状态通知redis
     * 异步通知
     */
    private class SyncSendRedisMessage implements Runnable {
        private long expId;
        private String actionType;
        private int action;

        SyncSendRedisMessage(long expId, String actionType, int action) {
            this.expId = expId;
            this.actionType = actionType;
            this.action = action;
        }

        @Override
        public void run() {
            logger.info("--> Send message to redis, expId={}, actionType={}, action={}", expId, actionType, action);
            List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);
            for (SensorInfo sensor : sensors) {
                String redisKey = GV.MONITOR.equals(actionType)? RedisKeyUtils.mkSensorMonitorKey():RedisKeyUtils.mkSensorRecordKey();
                if (action == 0){
                    boolean result = redisUtils.hashRemove(redisKey, sensor.getCode());
                    if (!result) {
                        logger.error("Redis action failure, alert!!!");
                    }
                } else if (action == 1){
                    String redisValue = new Gson().toJson(new SensorStatus(sensor.getCode(), action, sensor.getId(), sensor.getTrackId(), sensor.getSensorConfig().getId()));
                    boolean result = redisUtils.hashSet(redisKey, sensor.getCode(), redisValue);
                    if (!result) {
                        logger.error("Redis action failure, alert!!!");
                    }
                }
            }
        }
    }
}
