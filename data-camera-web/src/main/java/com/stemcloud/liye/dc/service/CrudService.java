package com.stemcloud.liye.dc.service;

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
import com.stemcloud.liye.dc.domain.config.SensorRegister;
import com.stemcloud.liye.dc.domain.data.ContentInfo;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public AppInfo saveApp(AppInfo app){
        return appRepository.save(app);
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
    // -------------------------------------------------

    // -------------------------------------------------
    /** 传感器组 **/
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
    // -------------------------------------------------

    // -------------------------------------------------
    /** 轨迹 **/
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
    // -------------------------------------------------

    // -------------------------------------------------
    /** 传感器 **/
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
    // -------------------------------------------------

    // -------------------------------------------------
    /** 数据片段 **/
    public RecorderInfo findRecorder(long id){
        return recorderRepository.findOne(id);
    }

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
    // -------------------------------------------------

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

    public List<ContentInfo> selectHotContent(){
        return contentRepository.findTop50ByIsSharedAndIsDeletedOrderByLikeDesc(1, 0);
    }

    public void deleteContent(long id){
        contentRepository.deleteContent(id);
    }

    // -------------------------------------------------
}
