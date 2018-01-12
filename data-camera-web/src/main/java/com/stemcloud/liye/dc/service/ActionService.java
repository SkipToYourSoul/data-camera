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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final VideoDataRepository videoDataRepository;
    private final AppRepository appRepository;

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
    public synchronized ExpStatus expCurrentStatus(long expId){
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
     * @param expId
     * @param action
     * @param isSave
     * @param dataTime
     * @param name
     * @param desc
     * @return 若有数据片段保存，则返回片段ID，否则返回-1
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized long changeMonitorState(long expId, int action, int isSave, long dataTime, String name, String desc) throws Exception {
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        long response = -1;

        if (action == 1){
            experimentRepository.monitorExp(expId, 1);
        } else if (action == 0){
            experimentRepository.monitorExp(expId, 0);
            experimentRepository.recorderExp(expId, 0);
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo != null) {
                if (isSave == 1){
                    String recorderName = name.isEmpty()?"实验{" + experiment.getName() + "}的片段":name;
                    String recorderDesc = desc.isEmpty()?"实验{" + experiment.getName() + "}的描述":desc;
                    recorderRepository.endRecorder(recorderInfo.getId(), new Date(dataTime), 0, recorderName, recorderDesc);
                    response = recorderInfo.getId();
                } else if (isSave == 0){
                    recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, recorderInfo.getName(), recorderInfo.getDescription());
                }
            }
        }

        logger.info("Change experiment monitor state, action={}, isSave={}, response={}", action, isSave, response);
        return response;
    }

    /**
     * 改变当前实验的录制状态
     * @param expId
     * @param action
     * @param isSave
     * @param dataTime
     * @param name
     * @param desc
     * @return 若有数据片段保存，则返回片段ID，否则返回-1
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized long changeRecorderState(long expId, int action, int isSave, long dataTime, String name, String desc) throws Exception {
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        long response = -1;

        if (action == 1){
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
            recorderInfo.setName("实验{" + experiment.getName() + "}的片段");
            recorderInfo.setDescription("实验{" + experiment.getName() + "}的描述");
            recorderRepository.save(recorderInfo);
        } else if (action == 0){
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo == null){
                throw new Exception("end record, but no record info in table");
            }
            experimentRepository.recorderExp(expId, 0);
            if (isSave == 1){
                String recorderName = name.isEmpty()?"实验{" + experiment.getName() + "}的片段":name;
                String recorderDesc = desc.isEmpty()?"实验{" + experiment.getName() + "}的描述":desc;
                recorderRepository.endRecorder(recorderInfo.getId(), new Date(dataTime), 0, recorderName, recorderDesc);
                saveVideo(recorderInfo);
                response = recorderInfo.getId();
            } else if (isSave == 0){
                recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, recorderInfo.getName(), recorderInfo.getDescription());
            }
        }

        logger.info("Change experiment record state, action={}, isSave={}, response={}", action, isSave, response);
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
                videoDataRepository.save(videoData);
                logger.info("Save video data, sensor id is {}, track id is {}, recorder is {}", sensorId, trackId, recorderInfo.getId());
            }
        }
    }

    /**
     * 获取当前场景的全局实验状态
     * @param appId
     * @return 6种状态
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized ExpStatus expAllStatus(long appId){
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
    public synchronized List<Long> allMonitor(long appId, int action, int isSave, long dataTime) throws Exception {
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
                    changeMonitorState(exp.getId(), action, isSave, dataTime, "", "");
                    expIds.add(exp.getId());
                    logger.info("Change experiment monitor state, action={}, isSave={}, expId={}", action, isSave, exp.getId());
                }
            }
        }

        return expIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized List<Long> allRecorder(long appId, int action, int isSave, long dataTime) throws Exception {
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
                boolean noChange = (action == 1 && exp.getIsRecorder() == 1) || (action == 0 && exp.getIsRecorder() == 0);
                if (!noChange){
                    changeRecorderState(exp.getId(), action, isSave, dataTime, "", "");
                    expIds.add(exp.getId());
                    logger.info("Change experiment record state, action={}, isSave={}, expId={}", action, isSave, exp.getId());
                }
            }
        }

        return expIds;
    }
}
