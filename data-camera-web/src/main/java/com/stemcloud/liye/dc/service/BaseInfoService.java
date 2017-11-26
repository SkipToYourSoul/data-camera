package com.stemcloud.liye.dc.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private final RecorderRepository recorderRepository;

    @Autowired
    public BaseInfoService(AppRepository appRepository, SensorRepository sensorRepository, ExperimentRepository experimentRepository, TrackRepository trackRepository, RecorderRepository recorderRepository) {
        this.appRepository = appRepository;
        this.sensorRepository = sensorRepository;
        this.experimentRepository = experimentRepository;
        this.trackRepository = trackRepository;
        this.recorderRepository = recorderRepository;
    }

    /** APPS **/
    public Map<Long, AppInfo> getOnlineApps(String user){
        List<AppInfo> apps = appRepository.findByCreatorAndIsDeletedOrderByCreateTime(user, 0);
        Map<Long, AppInfo> map = new HashMap<Long, AppInfo>(apps.size());
        for (AppInfo app: apps){
            map.put(app.getId(), app);
        }
        return map;
    }

    public Map<Long, ExperimentInfo> getOnlineExpOfApp(long id){
        AppInfo app = appRepository.findById(id);
        List<ExperimentInfo> experiments = experimentRepository.findByAppAndIsDeleted(app, 0);
        Map<Long, ExperimentInfo> map = new HashMap<Long, ExperimentInfo>(experiments.size());
        for (ExperimentInfo exp : experiments){
            map.put(exp.getId(), exp);
        }
        return map;
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

    /** SENSORS **/
    public List<SensorInfo> getOnlineSensor(String user){
        return sensorRepository.findByCreatorAndIsDeletedOrderByCreateTime(user, 0);
    }

    public List<ExperimentInfo> getOnlineExp(){
        return experimentRepository.findByIsDeletedOrderByCreateTime(0);
    }

    public List<TrackInfo> getOnlineTrack(){
        return trackRepository.findByIsDeletedOrderByCreateTime(0);
    }

    public List<SensorInfo> getAvailableSensorOfCurrentUser(String user){
        return sensorRepository.findByCreatorAndAppIdAndExpIdAndTrackId(user, 0, 0, 0);
    }

    public List<SensorInfo> getSensorsOfCurrentApp(long appId){
        return sensorRepository.findByAppId(appId);
    }

    /** RECORDERS **/
    public RecorderInfo getRecorderInfoOfExp(long expId){
        return recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
    }

    public Map<Long, List<RecorderInfo>> getAllRecordersOfCurrentApp(Map<Long, ExperimentInfo> experiments){
        Set<Long> expId = experiments.keySet();
        List<RecorderInfo> recorders = recorderRepository.findByExperiments(expId);
        Map<Long, List<RecorderInfo>> map = new HashMap<Long, List<RecorderInfo>>();
        for (RecorderInfo r : recorders){
            long eid = r.getExpId();
            List<RecorderInfo> list =  null;
            if (map.containsKey(eid)){
                list = map.get(eid);
            } else {
                list = new ArrayList<RecorderInfo>();
            }
            list.add(r);
            map.put(eid, list);
        }
        return map;
    }
}
