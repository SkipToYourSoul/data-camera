package com.stemcloud.liye.dc.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stemcloud.liye.dc.common.ExpStatus;
import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.dao.data.VideoDataRepository;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.common.SensorType;
import com.stemcloud.liye.dc.domain.data.RecorderDevices;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.data.VideoData;
import com.stemcloud.liye.dc.domain.message.SensorStatus;
import com.stemcloud.liye.dc.util.ExecutorUtil;
import com.stemcloud.liye.dc.util.RedisKeyUtils;
import com.stemcloud.liye.dc.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final VideoDataRepository videoDataRepository;
    private final AppRepository appRepository;

    @Autowired
    RedisUtils redisUtils;

    private Gson gson = new Gson();
    private final String MONITOR = "monitor";
    private final String RECORD = "record";

    public ActionService(ExperimentRepository experimentRepository, RecorderRepository recorderRepository, SensorRepository sensorRepository, VideoDataRepository videoDataRepository, AppRepository appRepository) {
        this.experimentRepository = experimentRepository;
        this.recorderRepository = recorderRepository;
        this.sensorRepository = sensorRepository;
        this.videoDataRepository = videoDataRepository;
        this.appRepository = appRepository;
    }

    /**
     * 返回从服务器获取的当前实验状态
     * @param expId
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
     * 改变当前实验的监控状态
     * @return 若有数据片段保存，则返回片段ID，否则返回-1
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public long changeMonitorState(long expId, int action, int isSave, long dataTime, String name, String desc) throws Exception {
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        long response = -1;

        if (action == 1){
            experimentRepository.monitorExp(expId, 1);
        } else if (action == 0){
            experimentRepository.monitorAndRecorderExp(expId, 0, 0);
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo != null) {
                if (isSave == 1){
                    recorderRepository.endRecorder(recorderInfo.getId(), new Date(dataTime), 0, name.isEmpty()?"实验{" + experiment.getName() + "}的片段":name, desc);
                    saveVideo(recorderInfo);
                    response = recorderInfo.getId();
                } else if (isSave == 0){
                    recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, recorderInfo.getName(), recorderInfo.getDescription());
                }
                ExecutorUtil.REDIS_EXECUTOR.submit(new SyncSendRedisMessage(expId, RECORD, 0));
            }
        }
        // -- send message
        // sendMessageToRedis(expId, MONITOR, action);
        ExecutorUtil.REDIS_EXECUTOR.submit(new SyncSendRedisMessage(expId, MONITOR, action));

        logger.info("--> Change experiment monitor state, action={}, isSave={}, response={}", action, isSave, response);
        return response;
    }

    /**
     * 改变当前实验的录制状态
     * @return 若有数据片段保存，则返回片段ID，否则返回-1
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public long changeRecorderState(long expId, int action, int isSave, long dataTime, String name, String desc) throws Exception {
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        long response = -1;

        if (action == 1){
            RecorderInfo testRecorder = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (testRecorder != null){
                logger.warn("The record has already start");
                return -1L;
            }

            experimentRepository.recorderExp(expId, 1);
            List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);

            // --- 新建一条片段记录
            List<RecorderDevices> devices = new ArrayList<RecorderDevices>();
            for (SensorInfo sensor: sensors){
                RecorderDevices device = new RecorderDevices();
                List<String> legend = Arrays.asList(sensor.getSensorConfig().getDimension().split(";"));
                device.setSensor(sensor.getId());
                device.setTrack(sensor.getTrackId());
                device.setLegends(legend);
                devices.add(device);
            }

            RecorderInfo recorderInfo = new RecorderInfo();
            recorderInfo.setExpId(expId);
            recorderInfo.setAppId(experiment.getApp().getId());
            recorderInfo.setIsRecorder(1);
            recorderInfo.setStartTime(new Date());
            recorderInfo.setDevices(new Gson().toJson(devices));
            recorderInfo.setName(experiment.getName());
            recorderInfo.setDescription(experiment.getName());
            recorderRepository.save(recorderInfo);
        } else if (action == 0){
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo == null){
                logger.warn("End record, but no record info in table");
                return -1L;
            }
            experimentRepository.recorderExp(expId, 0);
            if (isSave == 1){
                recorderRepository.endRecorder(recorderInfo.getId(), new Date(dataTime), 0, name.isEmpty()?"实验{" + experiment.getName() + "}的片段":name, desc);
                saveVideo(recorderInfo);
            } else if (isSave == 0){
                recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, recorderInfo.getName(), recorderInfo.getDescription());
            }
            response = recorderInfo.getId();
        }

        // -- send message
        // sendMessageToRedis(expId, RECORD, action);
        ExecutorUtil.REDIS_EXECUTOR.submit(new SyncSendRedisMessage(expId, RECORD, action));

        logger.info("--> Change experiment record state, action={}, isSave={}, response={}", action, isSave, response);
        return response;
    }

    /**
     * 在录制结束时，保存视频记录
     * @param recorderInfo 录制的片段信息
     */
    private void saveVideo(RecorderInfo recorderInfo){
        String devicesStr = recorderInfo.getDevices();
        List<RecorderDevices> devices = new Gson().fromJson(devicesStr, new TypeToken<ArrayList<RecorderDevices>>(){}.getType());
        for (RecorderDevices d : devices){
            long trackId = d.getTrack();
            long sensorId = d.getSensor();
            if (sensorRepository.findOne(sensorId).getSensorConfig().getType() == SensorType.VIDEO.getValue()){
                VideoData videoData = new VideoData();
                videoData.setSensorId(sensorId);
                videoData.setTrackId(trackId);
                videoData.setRecorderInfo(recorderInfo);

                // -- 保存上传的视频，由于上传具有延时性、后面会以异步的方式进行该表的更新
                videoData.setVideoPost("/camera/img/oceans.png");
                videoData.setVideoPath("/camera/img/oceans.mp4");

                videoDataRepository.save(videoData);
                logger.info("--> Save video data, sensor id is {}, track id is {}, recorder is {}", sensorId, trackId, recorderInfo.getId());
            }
        }
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
        } else if (!notInMonitorIds.isEmpty() && notInMonitorIds.size() == sensorExp){
            return ExpStatus.ALL_NOT_MONITOR;
        } else if (!notInMonitorIds.isEmpty()){
            return ExpStatus.PART_MONITORING;
        }

        return ExpStatus.UNKNOWN;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> allMonitor(long appId, int action, int isSave, long dataTime, String name, String desc) throws Exception {
        List<Long> expIds =  new ArrayList<Long>();
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
                    changeMonitorState(exp.getId(), action, isSave, dataTime, name, desc);
                    expIds.add(exp.getId());
                    logger.info("--> Global change experiment monitor state, action={}, isSave={}, expId={}", action, isSave, exp.getId());
                }
            }
        }

        return expIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> allRecorder(long appId, int action, int isSave, long dataTime, String name, String desc) throws Exception {
        List<Long> expIds =  new ArrayList<Long>();
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
                        changeRecorderState(exp.getId(), 0, isSave, dataTime, "", "");
                        changeRecorderState(exp.getId(), action, 0, 0, "", "");
                    } else {
                        changeRecorderState(exp.getId(), action, isSave, dataTime, name, desc);
                    }
                    expIds.add(exp.getId());
                    logger.info("--> Global change experiment record state, action={}, isSave={}, expId={}", action, isSave, exp.getId());
                }
            }
        }

        return expIds;
    }

    /**
     * 将传感器组的监控/录制状态通知redis
     * @param expId 传感器组ID
     * @param actionType monitor or recorder
     * @param action 0 or 1
     */
    private void sendMessageToRedis(long expId, String actionType, int action) throws Exception {
        logger.info("Send message to redis, expId={}, actionType={}, action={}", expId, actionType, action);
        List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);
        for (SensorInfo sensor : sensors) {
            String redisKey = MONITOR.equals(actionType)? RedisKeyUtils.mkSensorMonitorKey():RedisKeyUtils.mkSensorRecordKey();
            if (action == 0){
                boolean result = redisUtils.hashRemove(redisKey, sensor.getCode());
                if (!result) {
                    throw new Exception("Redis action failure, alert!!!");
                }
            } else if (action == 1){
                String redisValue = gson.toJson(new SensorStatus(sensor.getCode(), action, sensor.getId(), sensor.getTrackId(), sensor.getSensorConfig().getId()));
                boolean result = redisUtils.hashSet(redisKey, sensor.getCode(), redisValue);
                if (!result) {
                    throw new Exception("Redis action failure, alert!!!");
                }
            }
        }
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
                String redisKey = MONITOR.equals(actionType)? RedisKeyUtils.mkSensorMonitorKey():RedisKeyUtils.mkSensorRecordKey();
                if (action == 0){
                    boolean result = redisUtils.hashRemove(redisKey, sensor.getCode());
                    if (!result) {
                        logger.error("Redis action failure, alert!!!");
                    }
                } else if (action == 1){
                    String redisValue = gson.toJson(new SensorStatus(sensor.getCode(), action, sensor.getId(), sensor.getTrackId(), sensor.getSensorConfig().getId()));
                    boolean result = redisUtils.hashSet(redisKey, sensor.getCode(), redisValue);
                    if (!result) {
                        logger.error("Redis action failure, alert!!!");
                    }
                }
            }
        }
    }
}
