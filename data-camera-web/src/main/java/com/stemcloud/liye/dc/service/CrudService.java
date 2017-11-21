package com.stemcloud.liye.dc.service;

import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.base.TrackRepository;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Belongs to data-camera-web
 * Description:
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

    public CrudService(AppRepository appRepository, ExperimentRepository expRepository, TrackRepository trackRepository, SensorRepository sensorRepository) {
        this.appRepository = appRepository;
        this.expRepository = expRepository;
        this.trackRepository = trackRepository;
        this.sensorRepository = sensorRepository;
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
        AppInfo app = appRepository.findById(id);
        List<ExperimentInfo> experiments = expRepository.findByAppAndIsDeleted(app, 0);
        for (ExperimentInfo exp : experiments){
            deleteExp(exp.getId());
        }
    }

    public AppInfo findApp(Long id){
        return appRepository.findById(id);
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
        ExperimentInfo exp = expRepository.findById(id);
        Set<TrackInfo> tracks = exp.getTrackInfoList();
        for (TrackInfo track: tracks){
            if (track.getIsDeleted() == 0) {
                deleteTrack(track.getId());
            }
        }
    }

    public ExperimentInfo findExp(Long id){
        return expRepository.findById(id);
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
        logger.info("BOUND SENSOR " + sensorId + " ON TRACK " + trackId);
    }

    public void deleteTrack(Long id){
        int t = trackRepository.deleteTrack(id);
        logger.info("DELETE TRACK " + id);
        TrackInfo track = trackRepository.findById(id);
        // unbound sensor
        if (null != track.getSensor()){
            long sensorId = track.getSensor().getId();
            if (track.getSensor().getIsDeleted() == 0) {
                int s = sensorRepository.unboundSensorById(sensorId);
                logger.info("UNBOUND SENSOR " + sensorId);
            }
        }
    }

    public TrackInfo findTrack(Long id){
        return trackRepository.findById(id);
    }

    /************/
    /* SENSOR   */
    /************/
    public long saveSensor(SensorInfo sensor){
        return sensorRepository.save(sensor).getId();
    }

    public int deleteSensor(Long id){
        return sensorRepository.deleteSensor(id);
    }

    public SensorInfo findSensor(Long id){
        return sensorRepository.findById(id);
    }

    public void unboundSensor(long sensorId, long trackId){
        trackRepository.unboundSensor(trackId);
        sensorRepository.unboundSensorById(sensorId);
        logger.info("UNBOUND SENSOR " + sensorId + " ON TRACK " + trackId);
    }

    public void boundSensor(long sensorId, long trackId){
        TrackInfo trackInfo = trackRepository.findById(trackId);
        SensorInfo sensorInfo = sensorRepository.findById(sensorId);
        trackInfo.setSensor(sensorInfo);
        trackRepository.save(trackInfo);
        long expId = trackInfo.getExperiment().getId();
        long appId = trackInfo.getExperiment().getApp().getId();
        sensorRepository.boundSensor(sensorId, appId, expId, trackId);
        logger.info("BOUND SENSOR " + sensorId + " ON TRACK " + trackId);
    }

    public Integer changeSensorsMonitorStatusOfCurrentExperiment(long expId, int isMonitor){
        List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsMonitorAndIsDeleted(expId, isMonitor, 0);
        Set<Long> ids = new HashSet<Long>();
        for (SensorInfo sensor : sensors){
            ids.add(sensor.getId());
        }

        int s = 0;
        if (ids.size() > 0) {
            s = sensorRepository.monitorSensorByIds(ids, Math.abs(isMonitor - 1));
        }
        logger.info("CHANGE MONITOR STATUS OF SENSOR: " + s);
        return s;
    }

    public Integer changeSensorsRecorderStatusOfCurrentExperiment(long expId, int isRecorder){
        List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, isRecorder, 0);
        Set<Long> ids = new HashSet<Long>();
        for (SensorInfo sensor : sensors){
            ids.add(sensor.getId());
        }
        int s = 0;
        if (ids.size() > 0) {
            s = sensorRepository.recorderSensorByIds(ids, Math.abs(isRecorder - 1));
        }
        logger.info("CHANGE RECORDER STATUS OF SENSOR: " + s);
        return s;
    }
}
