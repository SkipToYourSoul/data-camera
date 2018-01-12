package com.stemcloud.liye.dc.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.base.TrackRepository;
import com.stemcloud.liye.dc.dao.config.SensorRegisterRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.dao.data.VideoDataRepository;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.common.RecordState;
import com.stemcloud.liye.dc.domain.config.SensorRegister;
import com.stemcloud.liye.dc.domain.data.RecorderDevices;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.data.VideoData;
import com.stemcloud.liye.dc.common.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  增、删、改、查
 *  crud operation of app, exp, track and sensor
 * @author liye on 2017/11/7
 */
@Service
public class CrudService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AppRepository appRepository;
    private final ExperimentRepository expRepository;
    private final TrackRepository trackRepository;
    private final SensorRepository sensorRepository;
    private final SensorRegisterRepository sensorRegisterRepository;
    private final RecorderRepository recorderRepository;
    private final VideoDataRepository videoDataRepository;

    public CrudService(AppRepository appRepository, ExperimentRepository expRepository, TrackRepository trackRepository, SensorRepository sensorRepository, SensorRegisterRepository sensorRegisterRepository, RecorderRepository recorderRepository, VideoDataRepository videoDataRepository) {
        this.appRepository = appRepository;
        this.expRepository = expRepository;
        this.trackRepository = trackRepository;
        this.sensorRepository = sensorRepository;
        this.sensorRegisterRepository = sensorRegisterRepository;
        this.recorderRepository = recorderRepository;
        this.videoDataRepository = videoDataRepository;
    }

    /*************/
    /* APP       */
    /*************/
    public long saveApp(AppInfo app){
        return appRepository.save(app).getId();
    }

    public void deleteApp(Long id){
        // delete app
        int a = appRepository.deleteApp(id);
        logger.info("DELETE APP " + id);
        AppInfo app = appRepository.findOne(id);
        List<ExperimentInfo> experiments = expRepository.findByAppAndIsDeletedOrderByCreateTime(app, 0);
        for (ExperimentInfo exp : experiments){
            deleteExp(exp.getId());
        }
    }

    public AppInfo findApp(Long id){
        return appRepository.findOne(id);
    }

    /**************/
    /* EXPERIMENT */
    /**************/
    public ExperimentInfo saveExp(ExperimentInfo exp){
        return expRepository.save(exp);
    }

    public void deleteExp(Long id){
        int e = expRepository.deleteExp(id);
        logger.info("DELETE EXPERIMENT " + id);
        ExperimentInfo exp = expRepository.findOne(id);
        Set<TrackInfo> tracks = exp.getTrackInfoList();
        for (TrackInfo track: tracks){
            if (track.getIsDeleted() == 0) {
                deleteTrack(track.getId());
            }
        }
    }

    public ExperimentInfo findExp(Long id){
        return expRepository.findOne(id);
    }

    /************/
    /* TRACK    */
    /************/
    public void newTrackAndBoundSensor(ExperimentInfo exp, SensorInfo sensor){
        TrackInfo track = new TrackInfo();
        track.setExperiment(exp);
        track.setSensor(sensor);
        track.setType(sensor.getSensorConfig().getType());
        TrackInfo trackInfo = trackRepository.save(track);
        long sensorId = trackInfo.getSensor().getId();
        long trackId = trackInfo.getId();
        long expId = trackInfo.getExperiment().getId();
        long appId = trackInfo.getExperiment().getApp().getId();
        sensorRepository.boundSensor(sensorId, appId, expId, trackId);
        logger.info("BOUND SENSOR {} ON TRACK {}.", sensorId, trackId);
    }

    public void deleteTrack(Long id){
        logger.info("DELETE TRACK {}", id);
        TrackInfo track = trackRepository.findOne(id);
        // unbound sensor
        if (null != track.getSensor()){
            long sensorId = track.getSensor().getId();
            if (track.getSensor().getIsDeleted() == 0) {
                int s = sensorRepository.unboundSensorById(sensorId);
                logger.info("UNBOUND SENSOR {}", sensorId);
            }
        }
        trackRepository.deleteTrack(id);
    }

    public TrackInfo findTrack(Long id){
        return trackRepository.findOne(id);
    }

    /************/
    /* SENSOR   */
    /************/
    public long saveSensor(SensorInfo sensor){
        return sensorRepository.save(sensor).getId();
    }

    public void deleteSensor(Long id, String code){
        sensorRepository.deleteSensor(id);
        sensorRegisterRepository.register(0, code);
    }

    public SensorInfo findSensor(Long id){
        return sensorRepository.findOne(id);
    }

    public void unboundSensor(long sensorId, long trackId){
        trackRepository.unboundSensor(trackId);
        sensorRepository.unboundSensorById(sensorId);
        logger.info("UNBOUND SENSOR {} ON TRACK {}.", sensorId, trackId);
    }

    public void boundSensor(long sensorId, long trackId){
        TrackInfo trackInfo = trackRepository.findOne(trackId);
        SensorInfo sensorInfo = sensorRepository.findOne(sensorId);
        trackInfo.setSensor(sensorInfo);
        trackRepository.save(trackInfo);
        long expId = trackInfo.getExperiment().getId();
        long appId = trackInfo.getExperiment().getApp().getId();
        sensorRepository.boundSensor(sensorId, appId, expId, trackId);
        logger.info("BOUND SENSOR {} ON TRACK {}.", sensorId, trackId);
    }

    public SensorRegister findRegister(String code){
        return sensorRegisterRepository.findByCode(code);
    }

    public void registerSensor(int action, String code){
        sensorRegisterRepository.register(action, code);
    }

    /************/
    /* RECORDER  */
    /************/
    public void updateRecorderName(long id, String name){
        recorderRepository.updateName(id, name);
    }

    public void updateRecorderDescription(long id, String description){
        recorderRepository.updateDescription(id, description);
    }

    public void deleteAllRecorder(long id){
        deleteRecorder(id);
        List<RecorderInfo> rs = recorderRepository.findByParentIdAndIsDeleted(id, 0);
        logger.info("delete {}, has child {}", id, rs.size());
        if (rs.size() == 0){
            return;
        }
        for (RecorderInfo r: rs){
            deleteAllRecorder(r.getId());
        }
    }

    private void deleteRecorder(long id){
        recorderRepository.deleteRecorder(id);
    }


    /************/
    /* MONIT AND RECORD   */
    /************/
    /**
     * 全局监控操作
     * @param appId 当前appId
     * @return 三种状态：1) 部分监控，提示用户不能进行操作; 2) 全部监控，停止全局监控; 3） 全部非监控，进入全局监控
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized Map allMonitor(long appId) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(2);

        List<ExperimentInfo> experiments = expRepository.findByAppAndIsDeletedOrderByCreateTime(appRepository.findOne(appId), 0);
        List<Long> notInMonitorIds = new ArrayList<Long>();
        // 选出当前绑定了设备，但是又没有处于监控状态的实验
        for (ExperimentInfo exp: experiments){
            Boolean hasSensor = false;
            for (TrackInfo track : exp.getTrackInfoList()){
                if (track.getSensor() != null){
                    hasSensor = true;
                    break;
                }
            }
            if (hasSensor && exp.getIsMonitor() == 0){
                notInMonitorIds.add(exp.getId());
            }
        }

        if (notInMonitorIds.isEmpty()){
            // 全部处于监控状态，关闭
            map.put("action", "close");
            for (ExperimentInfo exp: experiments) {
                expRepository.monitorExp(exp.getId(), 0);
                if (exp.getIsRecorder() == 1) {
                    // --- end recorder
                    RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(exp.getId(), 1, 0);
                    if (recorderInfo != null) {
                        recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, recorderInfo.getName(), recorderInfo.getDescription());
                        expRepository.recorderExp(exp.getId(), 0);
                    }
                }
            }
        } else {
            // 部分还没进入监控状态
            map.put("action", "open");
            for (Long expId : notInMonitorIds){
                expRepository.monitorExp(expId, 1);
            }
            map.put("ids", notInMonitorIds);
        }

        return map;
    }
}
