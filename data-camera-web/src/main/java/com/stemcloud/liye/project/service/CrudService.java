package com.stemcloud.liye.project.service;

import com.stemcloud.liye.project.dao.base.AppRepository;
import com.stemcloud.liye.project.dao.base.ExperimentRepository;
import com.stemcloud.liye.project.dao.base.SensorRepository;
import com.stemcloud.liye.project.dao.base.TrackRepository;
import com.stemcloud.liye.project.domain.base.AppInfo;
import com.stemcloud.liye.project.domain.base.ExperimentInfo;
import com.stemcloud.liye.project.domain.base.SensorInfo;
import com.stemcloud.liye.project.domain.base.TrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public long newApp(AppInfo app){
        return appRepository.save(app).getId();
    }

    public int updateApp(AppInfo app){
        return appRepository.updateApp(app.getId(), app.getName(), app.getDescription());
    }

    public void deleteApp(Long id){
        int a = appRepository.deleteApp(id);
        int s = sensorRepository.unboundSensorByApp(id);
        logger.info("MODIFY " + a + " RECORDER OF dc_base_app_info, MODIFY " + s + " RECORDER OF dc_base_sensor_info.");
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

    public int updateExp(ExperimentInfo exp){
        return expRepository.updateExp(exp.getId(), exp.getName(), exp.getDescription());
    }

    public int deleteExp(Long id){
        return expRepository.deleteExp(id);
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
        TrackInfo trackInfo = trackRepository.save(track);
        long sensorId = trackInfo.getSensor().getId();
        long trackId = trackInfo.getId();
        long expId = trackInfo.getExperiment().getId();
        long appId = trackInfo.getExperiment().getApp().getId();
        sensorRepository.boundSensor(sensorId, appId, expId, trackId);
    }

    public int deleteTrack(Long id){
        return trackRepository.deleteTrack(id);
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
}
