package com.stemcloud.liye.dc.service;

import com.stemcloud.liye.dc.common.SensorType;
import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.base.TrackRepository;
import com.stemcloud.liye.dc.dao.config.SensorRegisterRepository;
import com.stemcloud.liye.dc.dao.data.ContentRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.dao.data.VideoDataRepository;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.config.SensorConfig;
import com.stemcloud.liye.dc.domain.config.SensorRegister;
import com.stemcloud.liye.dc.domain.data.ContentInfo;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
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
    private final ContentRepository contentRepository;

    public CrudService(AppRepository appRepository, ExperimentRepository expRepository, TrackRepository trackRepository, SensorRepository sensorRepository, SensorRegisterRepository sensorRegisterRepository, RecorderRepository recorderRepository, VideoDataRepository videoDataRepository, ContentRepository contentRepository) {
        this.appRepository = appRepository;
        this.expRepository = expRepository;
        this.trackRepository = trackRepository;
        this.sensorRepository = sensorRepository;
        this.sensorRegisterRepository = sensorRegisterRepository;
        this.recorderRepository = recorderRepository;
        this.contentRepository = contentRepository;
    }

    // -------------------------------------------------
    /** 场景应用 **/
    @Transactional(rollbackFor = Exception.class)
    public AppInfo saveApp(String user, String name, String desc){
        AppInfo app = new AppInfo();
        app.setCreator(user);
        app.setName(name);
        app.setDescription(desc);
        return appRepository.save(app);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppInfo updateApp(long id, String name, String desc) {
        AppInfo app = appRepository.findOne(id);
        app.setName(name);
        app.setDescription(desc);
        return appRepository.save(app);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteApp(Long id){
        AppInfo app = appRepository.findOne(id);
        List<ExperimentInfo> experiments = expRepository.findByAppAndIsDeletedOrderByCreateTime(app, 0);
        for (ExperimentInfo exp : experiments){
            deleteExp(exp.getId());
        }
        appRepository.deleteApp(id);
    }

    public AppInfo findApp(Long id){
        return appRepository.findOne(id);
    }

    // -------------------------------------------------
    /** 传感器组 **/
    @Transactional(rollbackFor = Exception.class)
    public ExperimentInfo saveExp(long appId, String name, String desc, List<String> sensors){
        ExperimentInfo exp = new ExperimentInfo();
        exp.setName(name);
        exp.setDescription(desc);
        exp.setApp(appRepository.findOne(appId));
        expRepository.save(exp);

        if (null != sensors && sensors.size() > 0){
            for (String s: sensors){
                long sensorId = Long.parseLong(s);
                newTrackAndBoundSensor(exp, sensorRepository.findOne(sensorId));
            }
        }

        return exp;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExperimentInfo updateExp(long expId, String name, String desc, List<String> sensors) {
        ExperimentInfo exp = expRepository.findOne(expId);
        exp.setName(name);
        exp.setDescription(desc);
        expRepository.save(exp);

        if (null != sensors && sensors.size() > 0){
            for (String s: sensors){
                long sensorId = Long.parseLong(s);
                newTrackAndBoundSensor(exp, sensorRepository.findOne(sensorId));
            }
        }

        return exp;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteExp(Long id){
        ExperimentInfo exp = expRepository.findOne(id);
        Set<TrackInfo> tracks = exp.getTrackInfoList();
        for (TrackInfo track: tracks){
            if (track.getIsDeleted() == 0) {
                deleteTrack(track.getId());
            }
        }
        expRepository.deleteExp(id);
    }

    public ExperimentInfo findExp(Long id){
        return expRepository.findOne(id);
    }

    // -------------------------------------------------
    /** 轨迹 **/
    @Transactional(rollbackFor = Exception.class)
    public void newTrackAndBoundSensor(ExperimentInfo exp, SensorInfo sensor){
        TrackInfo track = new TrackInfo();
        track.setExperiment(exp);
        track.setSensor(sensor);
        track.setType(sensor.getSensorConfig().getType());
        TrackInfo trackInfo = trackRepository.save(track);
        logger.info("--> New Track {}", trackInfo.getId());
        long sensorId = trackInfo.getSensor().getId();
        long trackId = trackInfo.getId();
        long expId = trackInfo.getExperiment().getId();
        long appId = trackInfo.getExperiment().getApp().getId();
        sensorRepository.boundSensor(sensorId, appId, expId, trackId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTrack(Long id){
        logger.info("--> Delete Track {}", id);
        TrackInfo track = trackRepository.findOne(id);
        if (null != track.getSensor()){
            long sensorId = track.getSensor().getId();
            if (track.getSensor().getIsDeleted() == 0) {
                sensorRepository.unboundSensorById(sensorId);
            }
        }
        trackRepository.deleteTrack(id);
    }

    public TrackInfo findTrack(Long id){
        return trackRepository.findOne(id);
    }

    // -------------------------------------------------
    /** 传感器 **/
    @Transactional(rollbackFor = Exception.class)
    public SensorInfo saveSensor(String user, String code, SensorConfig config, String img, String name, String desc){
        SensorInfo sensor = new SensorInfo();
        sensor.setCode(code);
        sensor.setSensorConfig(config);
        if (!img.trim().isEmpty()){
            sensor.setImg(img);
        }
        sensor.setCreator(user);
        sensor.setName(name);
        sensor.setDescription(desc);

        // -- 更改传感器的注册状态
        registerSensor(1, code);

        // -- 如果是摄像头，则获取直播推流地址
        if (config.getType() == SensorType.VIDEO.getValue()) {
            SensorRegister register = sensorRegisterRepository.findByCode(code);
            sensor.setMark(register.getLive());
        }

        return sensorRepository.save(sensor);
    }

    @Transactional(rollbackFor = Exception.class)
    public SensorInfo updateSensor(Long id, String img, String name, String desc) {
        SensorInfo sensor = findSensor(id);
        if (!img.trim().isEmpty()){
            sensor.setImg(img);
        }
        sensor.setName(name);
        sensor.setDescription(desc);
        return sensorRepository.save(sensor);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSensor(Long id, String code){
        sensorRepository.deleteSensor(id);
        sensorRegisterRepository.register(0, code);
    }

    public SensorInfo findSensor(Long id){
        return sensorRepository.findOne(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unboundSensor(long sensorId, long trackId){
        trackRepository.unboundSensor(trackId);
        sensorRepository.unboundSensorById(sensorId);
        logger.info("--> Unbound Sensor {} On Track {}.", sensorId, trackId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void boundSensor(long sensorId, long trackId){
        TrackInfo trackInfo = trackRepository.findOne(trackId);
        SensorInfo sensorInfo = sensorRepository.findOne(sensorId);
        trackInfo.setSensor(sensorInfo);
        trackRepository.save(trackInfo);
        long expId = trackInfo.getExperiment().getId();
        long appId = trackInfo.getExperiment().getApp().getId();
        sensorRepository.boundSensor(sensorId, appId, expId, trackId);
        logger.info("--> Bound Sensor {} On Track {}.", sensorId, trackId);
    }

    public SensorRegister findRegister(String code){
        return sensorRegisterRepository.findByCode(code);
    }

    public void registerSensor(int action, String code){
        sensorRegisterRepository.register(action, code);
    }

    // -------------------------------------------------
    /** 数据片段 **/
    public RecorderInfo findRecorder(long id){
        return recorderRepository.findOne(id);
    }

    public void updateRecorderName(long id, String name){
        recorderRepository.updateName(id, name);
    }

    public void updateRecorderDescription(long id, String title, String description){
        recorderRepository.updateDescription(id, title, description);
    }

    public void deleteAllRecorder(long id){
        deleteRecorder(id);
        List<RecorderInfo> rs = recorderRepository.findByParentIdAndIsDeleted(id, 0);
        if (rs.size() == 0){
            return;
        }
        for (RecorderInfo r: rs){
            deleteAllRecorder(r.getId());
        }
    }

    private void deleteRecorder(long id){
        logger.info("--> Delete recorder {}", id);
        recorderRepository.deleteRecorder(id);
    }

    // -------------------------------------------------
    /** 内容 **/
    public ContentInfo saveContent(String user, String name, String desc, String category, String tag, int isShared, long recorderId, String img){
        ContentInfo content = new ContentInfo();
        content.setOwner(user);
        content.setTitle(name);
        content.setDescription(desc);
        content.setCategory(category);
        content.setTag(tag);
        content.setIsShared(isShared);
        content.setRecorderInfo(recorderRepository.findOne(recorderId));
        if (!img.trim().isEmpty()){
            content.setImg(img);
        }
        return contentRepository.save(content);
    }

    public ContentInfo findContent(long id){
        return contentRepository.findOne(id);
    }

    public List<ContentInfo> selectUserContent(String user){
        return contentRepository.findByOwnerAndIsDeleted(user, 0);
    }

    public List<ContentInfo> selectUserHotContent(String user){
        return contentRepository.findByOwnerAndIsDeletedOrderByLikeDesc(user, 0);
    }

    public List<ContentInfo> selectHotContent(){
        return contentRepository.findTop50ByIsSharedAndIsDeletedOrderByLikeAndCreateTimeDesc();
    }

    public void deleteContent(long id){
        contentRepository.deleteContent(id);
    }
}
