package com.stemcloud.liye.dc.service;

import com.google.gson.Gson;
import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.base.TrackRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private final RecorderRepository recorderRepository;

    public CrudService(AppRepository appRepository, ExperimentRepository expRepository, TrackRepository trackRepository, SensorRepository sensorRepository, RecorderRepository recorderRepository) {
        this.appRepository = appRepository;
        this.expRepository = expRepository;
        this.trackRepository = trackRepository;
        this.sensorRepository = sensorRepository;
        this.recorderRepository = recorderRepository;
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

    /************/
    /* MONITOR AND RECORDER   */
    /************/
    public synchronized Integer changeSensorsMonitorStatusOfCurrentExperiment(long expId) throws Exception {
        // --- check the monitor status of current exp
        ExperimentInfo exp = expRepository.findById(expId);
        List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);
        int status = exp.getIsMonitor();
        int response = -1;
        if (sensors.size() == 0){
            logger.warn("NO SENSORS BOUND FOR EXPERIMENT {}, RETURN -1", expId);
            return response;
        }

        if (status == 0){
            // --- not in monitor state
            expRepository.monitorExp(expId, 1);
            response = 1;
        } else if (status == 1){
            // --- in monitor state
            expRepository.monitorExp(expId, 0);
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo == null){
                throw new Exception("end record, but no record info in table");
            }
            recorderRepository.endRecorder(recorderInfo.getId(), new Date());
            response = 0;
        }
        logger.info("CHANGE MONITOR STATUS OF EXPERIMENT {} from {} to {}", expId, status, Math.abs(status - 1));
        return response;
    }

    public synchronized Integer changeSensorsRecorderStatusOfCurrentExperiment(long expId) throws Exception {
        // --- check the recorder status of current exp
        ExperimentInfo exp = expRepository.findById(expId);
        int status = exp.getIsRecorder();
        int response = -1;
        List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);
        if (sensors.size() == 0){
            logger.warn("NO SENSORS BOUND FOR EXPERIMENT {}, RETURN -1", expId);
            return response;
        }

        if (status == 0){
            // --- not in recorder state
            expRepository.recorderExp(expId, 1);

            // --- new a recorder info
            Map<String, List<Long>> devices = new HashMap<String, List<Long>>(){{
                put("sensors", new ArrayList<Long>());
                put("tracks", new ArrayList<Long>());
            }};
            for (SensorInfo sensor: sensors){
                long sensorId = sensor.getId();
                long trackId = sensor.getTrackId();
                List<Long> s = devices.get("sensors");
                s.add(sensorId);
                devices.put("sensors", s);
                List<Long> t = devices.get("tracks");
                t.add(trackId);
                devices.put("tracks", t);
            }

            RecorderInfo recorderInfo = new RecorderInfo();
            recorderInfo.setExpId(expId);
            recorderInfo.setStartTime(new Date());
            recorderInfo.setDevices(new Gson().toJson(devices));
            recorderRepository.save(recorderInfo);

            response = 1;
        } else if (status == 1){
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo == null){
                throw new Exception("end record, but no record info in table");
            }
            recorderRepository.endRecorder(recorderInfo.getId(), new Date());

            response = 0;
        }

        logger.info("CHANGE RECORDER STATUS OF EXPERIMENT {} from {} to {}", expId, status, Math.abs(status - 1));
        return response;
    }
}
