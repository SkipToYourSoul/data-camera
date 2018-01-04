package com.stemcloud.liye.dc.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.base.TrackRepository;
import com.stemcloud.liye.dc.dao.config.SensorRegisterRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.dao.data.VideoDataRepository;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.common.RecordState;
import com.stemcloud.liye.dc.domain.config.SensorRegister;
import com.stemcloud.liye.dc.domain.data.RecorderDevices;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.data.VideoData;
import com.stemcloud.liye.dc.domain.common.SensorType;
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
    private final VideoDataRepository videoDataRepository;

    public CrudService(AppRepository appRepository, ExperimentRepository expRepository, TrackRepository trackRepository, SensorRepository sensorRepository, SensorRegisterRepository sensorRegisterRepository, RecorderRepository recorderRepository, VideoDataRepository videoDataRepository) {
        this.appRepository = appRepository;
        this.expRepository = expRepository;
        this.trackRepository = trackRepository;
        this.sensorRepository = sensorRepository;
        this.sensorRegisterRepository = sensorRegisterRepository;
        this.recorderRepository = recorderRepository;
        this.videoDataRepository = videoDataRepository;
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
        AppInfo app = appRepository.findOne(id);
        List<ExperimentInfo> experiments = expRepository.findByAppAndIsDeletedOrderByCreateTime(app, 0);
        for (ExperimentInfo exp : experiments){
            deleteExp(exp.getId());
        }
    }

    public AppInfo findApp(Long id){
        return appRepository.findOne(id);
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
        logger.info("BOUND SENSOR {} ON TRACK {}.", sensorId, trackId);
    }

    public void deleteTrack(Long id){
        int t = trackRepository.deleteTrack(id);
        logger.info("DELETE TRACK {}", id);
        TrackInfo track = trackRepository.findById(id);
        // unbound sensor
        if (null != track.getSensor()){
            long sensorId = track.getSensor().getId();
            if (track.getSensor().getIsDeleted() == 0) {
                int s = sensorRepository.unboundSensorById(sensorId);
                logger.info("UNBOUND SENSOR {}", sensorId);
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

    public void deleteSensor(Long id, String code){
        sensorRepository.deleteSensor(id);
        sensorRegisterRepository.register(0, code);
    }

    public SensorInfo findSensor(Long id){
        return sensorRepository.findById(id);
    }

    public void unboundSensor(long sensorId, long trackId){
        trackRepository.unboundSensor(trackId);
        sensorRepository.unboundSensorById(sensorId);
        logger.info("UNBOUND SENSOR {} ON TRACK {}.", sensorId, trackId);
    }

    public void boundSensor(long sensorId, long trackId){
        TrackInfo trackInfo = trackRepository.findById(trackId);
        SensorInfo sensorInfo = sensorRepository.findById(sensorId);
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

    /************/
    /* RECORDER  */
    /************/
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


    /************/
    /* MONIT AND RECORD   */
    /************/
    @Transactional(rollbackFor = Exception.class)
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
            if (exp.getIsRecorder() == 1) {
                // --- end recorder
                RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
                if (recorderInfo == null) {
                    throw new Exception("end record, but no record info in table");
                }
                recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, recorderInfo.getName(), recorderInfo.getDescription());
                expRepository.recorderExp(expId, 0);
            }
            response = 0;
        }
        logger.info("CHANGE MONITOR STATUS OF EXPERIMENT {} from {} to {}", expId, status, Math.abs(status - 1));
        return response;
    }

    /**
     *  录制操作，开始录制时，新建片段信息；结束操作时，为当前片段信息加上结束时间
     * @param expId 实验id
     * @param isSave 是否保存实验片段
     * @param name 数据片段名
     * @param desc 数据片段描述
     * @param timestamp 数据片段时间
     * @return {
     *     -10：实验没有绑定传感器
     *     -1：开始录制
     *     0: 结束录制未保存结果
     *     ?>0：结束录制并保存结果，返回recorder id
     * }
     * @throws Exception 若抛出异常，则回滚
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized Long changeSensorsRecorderStatusOfCurrentExperiment(long appId, long expId, int isSave, String name, String desc, Long timestamp) throws Exception {
        // --- check the recorder status of current exp
        ExperimentInfo exp = expRepository.findById(expId);
        int status = exp.getIsRecorder();
        List<SensorInfo> sensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);
        if (sensors.size() == 0){
            logger.warn("NO SENSORS BOUND FOR EXPERIMENT {}, RETURN -1", expId);
            return RecordState.ERR.getValue();
        }

        if (status == 0){
            // --- 处于非录制状态，切换为录制状态
            expRepository.recorderExp(expId, 1);

            // --- new a recorder info
            List<RecorderDevices> devices = new ArrayList<RecorderDevices>();
            for (SensorInfo sensor: sensors){
                RecorderDevices device = new RecorderDevices();
                long sensorId = sensor.getId();
                long trackId = sensor.getTrackId();
                List<String> legend = Arrays.asList(sensor.getSensorConfig().getDimension().split(";"));
                device.setSensor(sensorId);
                device.setTrack(trackId);
                device.setLegends(legend);
                devices.add(device);
            }

            RecorderInfo recorderInfo = new RecorderInfo();
            recorderInfo.setExpId(expId);
            recorderInfo.setAppId(appId);
            recorderInfo.setIsRecorder(1);
            recorderInfo.setStartTime(new Date());
            recorderInfo.setDevices(new Gson().toJson(devices));
            recorderInfo.setName(exp.getName() + "-新片段");
            recorderInfo.setDescription(exp.getName() + "的记录描述");
            recorderRepository.save(recorderInfo);

            logger.info("CHANGE RECORDER STATUS OF EXPERIMENT {} from {} to {}", expId, status, Math.abs(status - 1));
            return RecordState.ING.getValue();
        } else if (status == 1){
            // --- end recorder
            RecorderInfo recorderInfo = recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
            if (recorderInfo == null){
                throw new Exception("end record, but no record info in table");
            }
            expRepository.recorderExp(expId, 0);
            if (name.trim().isEmpty()){
                name = recorderInfo.getName();
            }
            if (desc.trim().isEmpty()){
                desc = recorderInfo.getDescription();
            }

            if (isSave == 1){
                // save data
                recorderRepository.endRecorder(recorderInfo.getId(), new Date(timestamp), 0, name, desc);
                // save video if have
                saveVideo(recorderInfo);
                logger.info("CHANGE RECORDER STATUS OF EXPERIMENT {} from {} to {}", expId, status, Math.abs(status - 1));
                return recorderInfo.getId();
            } else {
                // not save data, delete recorder
                recorderRepository.endRecorder(recorderInfo.getId(), new Date(), 1, name, desc);
                logger.info("CHANGE RECORDER STATUS OF EXPERIMENT {} from {} to {}", expId, status, Math.abs(status - 1));
                return RecordState.END.getValue();
            }
        }
        return RecordState.ERR.getValue();
    }

    /**-------**/
    /* VIDEO   */
    /**-------**/

    /**
     * 在录制结束时，保存视频记录
     *
     * @param recorderInfo 录制的片段信息
     */
    private void saveVideo(RecorderInfo recorderInfo){
        String devicesStr = recorderInfo.getDevices();
        List<RecorderDevices> devices = new Gson().fromJson(devicesStr, new TypeToken<ArrayList<RecorderDevices>>(){}.getType());
        for (RecorderDevices d : devices){
            long trackId = d.getTrack();
            long sensorId = d.getSensor();
            if (findSensor(sensorId).getSensorConfig().getType() == SensorType.VIDEO.getValue()){
                VideoData videoData = new VideoData();
                videoData.setSensorId(sensorId);
                videoData.setTrackId(trackId);
                videoData.setRecorderInfo(recorderInfo);
                videoDataRepository.save(videoData);
                logger.info("SAVE VIDEO DATA, SENSOR ID IS {}, TRACK ID IS {}, RECORDER IS {}", sensorId, trackId, recorderInfo.getId());
            }
        }
    }
}
