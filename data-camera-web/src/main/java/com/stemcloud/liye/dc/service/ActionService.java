package com.stemcloud.liye.dc.service;

import com.stemcloud.liye.dc.common.ExpStatus;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import org.springframework.stereotype.Service;

/**
 * Belongs to data-camera-web
 * Description:
 *  实验的监控、录制操作
 * @author liye on 2018/1/11
 */
@Service
public class ActionService {
    private final ExperimentRepository experimentRepository;

    public ActionService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
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

}
