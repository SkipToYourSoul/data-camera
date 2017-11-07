package com.stemcloud.liye.project.service;

import com.stemcloud.liye.project.dao.base.AppRepository;
import com.stemcloud.liye.project.dao.base.ExperimentRepository;
import com.stemcloud.liye.project.dao.base.SensorRepository;
import com.stemcloud.liye.project.dao.base.TrackRepository;
import com.stemcloud.liye.project.domain.base.AppInfo;
import com.stemcloud.liye.project.domain.base.ExperimentInfo;
import com.stemcloud.liye.project.domain.base.SensorInfo;
import com.stemcloud.liye.project.domain.base.TrackInfo;
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
}
