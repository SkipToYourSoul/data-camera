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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  service for base domain
 * @author liye on 2017/11/6
 */
@Service
public class BaseInfoService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AppRepository appRepository;
    private final SensorRepository sensorRepository;
    private final ExperimentRepository experimentRepository;
    private final TrackRepository trackRepository;

    @Autowired
    public BaseInfoService(AppRepository appRepository, SensorRepository sensorRepository, ExperimentRepository experimentRepository, TrackRepository trackRepository) {
        this.appRepository = appRepository;
        this.sensorRepository = sensorRepository;
        this.experimentRepository = experimentRepository;
        this.trackRepository = trackRepository;
    }

    public List<AppInfo> getOnlineApps(String user){
        return appRepository.findByCreatorAndIsDeletedOrderByCreateTime(user, 0);
    }

    public List<SensorInfo> getOnlineSensor(String user){
        return sensorRepository.findByCreatorAndIsDeletedOrderByCreateTime(user, 0);
    }

    public List<ExperimentInfo> getOnlineExp(){
        return experimentRepository.findByIsDeletedOrderByCreateTime(0);
    }

    public List<TrackInfo> getOnlineTrack(){
        return trackRepository.findByIsDeletedOrderByCreateTime(0);
    }

    public Boolean isAppBelongUser(long id, String user) {
        AppInfo app = appRepository.findById(id);
        if (app == null){
            logger.warn("null app " + id);
            return false;
        }
        logger.info("compare user " + user + " with app creator " + app.getCreator());
        return app.getIsDeleted() == 0 && user.equals(app.getCreator());
    }

    public AppInfo getCurrentApp(long id){
        return appRepository.findById(id);
    }

    public List<ExperimentInfo> getOnlineExpOfApp(long id){
        AppInfo app = appRepository.findById(id);
        return experimentRepository.findByAppAndIsDeleted(app, 0);
    }

    public List<SensorInfo> getAvailableSensorOfCurrentUser(String user){
        return sensorRepository.findByCreatorAndAppIdAndExpIdAndTrackId(user, 0, 0, 0);
    }

    public List<SensorInfo> getSensorsOfCurrentApp(long appId){
        return sensorRepository.findByAppId(appId);
    }
}
