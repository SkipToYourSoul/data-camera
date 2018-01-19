package com.stemcloud.liye.dc.service;

import com.stemcloud.liye.dc.dao.base.AppRepository;
import com.stemcloud.liye.dc.dao.base.ExperimentRepository;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.base.TrackRepository;
import com.stemcloud.liye.dc.dao.data.ContentRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.data.ContentInfo;
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
    private final ContentRepository contentRepository;

    @Autowired
    public BaseInfoService(AppRepository appRepository, SensorRepository sensorRepository, ExperimentRepository experimentRepository, TrackRepository trackRepository, RecorderRepository recorderRepository, ContentRepository contentRepository) {
        this.appRepository = appRepository;
        this.sensorRepository = sensorRepository;
        this.experimentRepository = experimentRepository;
        this.trackRepository = trackRepository;
        this.recorderRepository = recorderRepository;
        this.contentRepository = contentRepository;
    }

    // --- APPS
    /**
     * 获取用户当前的应用，按创建时间排序
     * @param user 当前用户
     * @return Map
     */
    public Map<Long, AppInfo> getOnlineApps(String user){
        List<AppInfo> apps = appRepository.findByCreatorAndIsDeletedOrderByCreateTimeDesc(user, 0);
        Map<Long, AppInfo> map = new LinkedHashMap<Long, AppInfo>(apps.size());
        for (AppInfo app: apps){
            map.put(app.getId(), app);
        }
        return map;
    }

    /**
     * 判断当前用户是否拥有访问的应用页面的权限
     * @param id 应用id
     * @param user 用户
     * @return true or false
     */
    public Boolean isAppBelongUser(long id, String user) {
        AppInfo app = appRepository.findOne(id);
        logger.info("Check user {} has app {}", user, id);
        return app != null && app.getIsDeleted() == 0 && user.equals(app.getCreator());
    }

    /** EXPERIMENT **/
    /**
     * 获取当前应用下的在线实验数据，按创建时间排序
     * @param id 应用id
     * @return Map
     */
    public Map<Long, ExperimentInfo> getOnlineExpOfApp(long id){
        AppInfo app = appRepository.findOne(id);
        List<ExperimentInfo> experiments = experimentRepository.findByAppAndIsDeletedOrderByCreateTime(app, 0);
        Map<Long, ExperimentInfo> map = new LinkedHashMap<Long, ExperimentInfo>(experiments.size());
        for (ExperimentInfo exp : experiments){
            map.put(exp.getId(), exp);
        }
        return map;
    }

    /**
     * 获取有效的实验数据
     * @return
     */
    public List<ExperimentInfo> getOnlineExp(){
        return experimentRepository.findByIsDeletedOrderByCreateTime(0);
    }

    // --- SENSORS
    /**
     * 获取有效的设备数据
     * @param user
     * @return
     */
    public List<SensorInfo> getOnlineSensor(String user){
        return sensorRepository.findByCreatorAndIsDeletedOrderByCreateTime(user, 0);
    }

    /**
     * 获取当前用户下未被绑定的设备
     * @param user 用户
     * @return
     */
    public Map<Long, SensorInfo> getAvailableSensorOfCurrentUser(String user){
        List<SensorInfo> list = sensorRepository.findByCreatorAndAppIdAndExpIdAndTrackId(user, 0, 0, 0);
        Map<Long, SensorInfo> map = new HashMap<Long, SensorInfo>();
        for (SensorInfo sensor: list){
            map.put(sensor.getId(), sensor);
        }
        return map;
    }

    // --- TRACK
    /**
     * 获取有效的轨迹数据
     * @return
     */
    public List<TrackInfo> getOnlineTrack(){
        return trackRepository.findByIsDeletedOrderByCreateTime(0);
    }

    // --- RECORDERS
    /**
     * 获取正在录制的实验的录制信息（一个实验最多只有一条正在录制的信息）
     * @param expId 实验ID
     * @return recorder
     */
    public RecorderInfo getRecorderInfoOfExp(long expId){
        return recorderRepository.findByExpIdAndIsRecorderAndIsDeleted(expId, 1, 0);
    }

    /**
     * 获取当前应用下的所有实验记录，按创建时间排序
     * @return Map<appId-Recorder>
     */
    public Map<Long, List<RecorderInfo>> getAllRecorders(Map<Long, AppInfo> apps){
        List<RecorderInfo> recorders = recorderRepository.findByIsDeletedOrderByIdDesc(0);
        Map<Long, List<RecorderInfo>> map = new LinkedHashMap<Long, List<RecorderInfo>>();
        for (Map.Entry<Long, AppInfo> entry : apps.entrySet()){
            long appId = entry.getKey();
            for (RecorderInfo r : recorders){
                if (appId == r.getAppId()){
                    List<RecorderInfo> list;
                    if (map.containsKey(appId)){
                        list = map.get(appId);
                    } else {
                        list = new ArrayList<RecorderInfo>();
                    }
                    list.add(r);
                    map.put(appId, list);
                }
            }
        }

        return map;
    }

    /**
     * 获取当前应用下的所有实验记录
     * @param experiments 应用中的实验
     * @return Map<expId-Recorder>
     */
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

    // ---- 内容
    /**
     * 判断当前用户是否拥有访问的应用页面的权限
     * @param id 内容id
     * @param user 用户
     * @return true or false
     */
    public Boolean isContentBelongUser(long id, String user) {
        ContentInfo content = contentRepository.findOne(id);
        logger.info("Check user {} has content {}", user, id);
        return content != null && content.getIsDeleted() == 0 && user.equals(content.getOwner());
    }
}
