package com.stemcloud.liye.dc.service;

import com.stemcloud.liye.dc.common.ExpStatus;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *  实验的监控、录制操作
 * @author liye on 2018/1/11
 */
@Service
public class ActionService {
    private final ExperimentRepository experimentRepository;
    private final RecorderRepository recorderRepository;

    public ActionService(ExperimentRepository experimentRepository, RecorderRepository recorderRepository) {
        this.experimentRepository = experimentRepository;
        this.recorderRepository = recorderRepository;
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

    /**
     * 改变当前实验的监控状态
     * @param expId
     * @param action
     * @param isSave
     * @param dataTime
     * @param name
     * @param desc
     * @return 若有数据片段保存，则返回片段ID，否则返回-1
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized long changeMonitorState(long expId, int action, int isSave, long dataTime, String name, String desc) throws Exception {
        ExperimentInfo experiment = experimentRepository.findOne(expId);
        long response = -1;

        if (action == 1){
            experimentRepository.monitorExp(expId, 1);
        } else if (action == 0){
            experimentRepository.monitorExp(expId, 0);
            experimentRepository.recorderExp(expId, 0);
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo != null) {
                if (isSave == 1){
                    String recorderName = name.isEmpty()?"实验{" + experiment.getName() + "}的片段":name;
                    String recorderDesc = desc.isEmpty()?"实验{" + experiment.getName() + "}的描述":desc;
                    recorderRepository.endRecorder(recorderInfo.getId(), new Date(dataTime), 0, recorderName, recorderDesc);
                    response = recorderInfo.getId();
                } else if (isSave == 0){
                    recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, recorderInfo.getName(), recorderInfo.getDescription());
                }
            }
        }

        return response;
    }
}
