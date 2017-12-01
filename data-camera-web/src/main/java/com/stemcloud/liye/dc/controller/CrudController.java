package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.common.ServerReturnTool;
import com.stemcloud.liye.dc.service.CommonService;
import com.stemcloud.liye.dc.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  增加(Create)、读取查询(Retrieve)、更新(Update)、删除(Delete)
 *  for app, experiment, track and sensor
 * @author liye on 2017/11/6
 */
@RestController
@RequestMapping("/crud")
public class CrudController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonService commonService;
    private final CrudService crudService;

    @Autowired
    public CrudController(CommonService commonService, CrudService crudService) {
        this.commonService = commonService;
        this.crudService = crudService;
    }

    /* new or modify app */
    @PostMapping("/app/update")
    public Long updateApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        String appId = "app-id";
        String appName = "app-name";
        String appDesc = "app-desc";
        AppInfo appInfo = null;
        String user = commonService.getCurrentLoginUser(request);
        if (!queryParams.containsKey(appId)){
            appInfo = new AppInfo();
        } else {
            Long id = Long.parseLong(queryParams.get("app-id"));
            appInfo = crudService.findApp(id);
        }
        if (null == appInfo){
            throw new IllegalArgumentException("ERROR PARAMETERS WHEN UPDATE APP");
        }

        appInfo.setCreator(user);
        if (queryParams.containsKey(appName) && !queryParams.get(appName).trim().isEmpty()) {
            appInfo.setName(queryParams.get(appName));
        }
        if (queryParams.containsKey(appDesc) && !queryParams.get(appDesc).trim().isEmpty()) {
            appInfo.setDescription(queryParams.get(appDesc));
        }
        Long id = crudService.saveApp(appInfo);
        logger.info("USER " + user + " UPDATE APP " + id);

        return id;
    }

    /* delete app */
    @GetMapping("/app/delete")
    public String deleteApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        Long id = Long.parseLong(queryParams.get("app-id"));
        String user = commonService.getCurrentLoginUser(request);
        if (id <= 0){
            throw new IllegalArgumentException("ERROR PARAMETERS WHEN DELETE APP");
        }
        logger.info("USER " + user + " DELETE APP " + id);
        crudService.deleteApp(id);
        return "SUCCESS";
    }

    /* new or modify experiment */
    @PostMapping("/exp/update")
    public String updateExp(@RequestParam Map<String, String> queryParams, @RequestParam(value = "exp-select") List<String> sensors, HttpServletRequest request){
        String expId = "exp-id";
        String expName = "exp-name";
        String expDesc = "exp-desc";
        ExperimentInfo expInfo = null;
        String user = commonService.getCurrentLoginUser(request);

        if (!queryParams.containsKey(expId)){
            expInfo = new ExperimentInfo();
        } else {
            Long id = Long.parseLong(queryParams.get("exp-id"));
            expInfo = crudService.findExp(id);
        }
        if (null == expInfo){
            throw new IllegalArgumentException("ERROR PARAMETERS WHEN UPDATE EXPERIMENT");
        }

        expInfo.setApp(crudService.findApp(Long.valueOf(queryParams.get("app-id"))));
        if (queryParams.containsKey(expName) && !queryParams.get(expName).trim().isEmpty()) {
            expInfo.setName(queryParams.get(expName));
        }
        if (queryParams.containsKey(expDesc) && !queryParams.get(expDesc).trim().isEmpty()) {
            expInfo.setDescription(queryParams.get(expDesc));
        }
        ExperimentInfo newExpInfo = crudService.saveExp(expInfo);

        // add sensor on experiment
        if (sensors.size() > 0){
            for (String s: sensors){
                long sensorId = Long.parseLong(s);
                crudService.newTrackAndBoundSensor(newExpInfo, crudService.findSensor(sensorId));
            }
        }
        logger.info("USER " + user + " UPDATE EXP " + newExpInfo.getId() + ", ADD " + sensors.size() + " SENSORS TO THE EXP.");

        return "SUCCESS";
    }

    /* delete experiment */
    @GetMapping("/exp/delete")
    public String deleteExp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        Long id = Long.parseLong(queryParams.get("exp-id"));
        String user = commonService.getCurrentLoginUser(request);
        if (id <= 0){
            throw new IllegalArgumentException("ERROR PARAMETERS WHEN DELETE EXPERIMENT");
        }
        logger.info("USER " + user + " DELETE EXP " + id);
        crudService.deleteExp(id);

        return "SUCCESS";
    }

    /* bound/unbound sensor on track */
    @PostMapping("/bound/toggle")
    public Map<String, String> boundToggle(@RequestParam Map<String, String> queryParams){
        Map<String, String> result = new HashMap<String, String>();

        long trackId = Long.parseLong(queryParams.get("pk"));
        String dom = queryParams.get("name");
        if (queryParams.get("value").isEmpty()){
            // unbound
            TrackInfo track = crudService.findTrack(trackId);
            long sensorId = track.getSensor().getId();
            crudService.unboundSensor(sensorId, trackId);

            result.put("action", "unbound");
            result.put("sensor", String.valueOf(sensorId));
        } else {
            // bound
            long sensorId = Long.parseLong(queryParams.get("value"));
            crudService.boundSensor(sensorId, trackId);

            result.put("action", "bound");
            result.put("sensor", String.valueOf(sensorId));
        }
        result.put("dom", dom);

        return result;
    }

    /* delete track */
    @GetMapping("/track/delete")
    public String deleteTrack(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        String user = commonService.getCurrentLoginUser(request);
        Long trackId = Long.parseLong(queryParams.get("track-id"));
        if (trackId <= 0){
            throw new IllegalArgumentException("ERROR PARAMETERS WHEN DELETE TRACK");
        }
        logger.info("USER " + user + " DELETE TRACK " + trackId);
        crudService.deleteTrack(trackId);

        return "SUCCESS";
    }

    @GetMapping("/sensor/new")
    public Long newSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        SensorInfo sensor = new SensorInfo();
        String user = commonService.getCurrentLoginUser(request);

        String sensorName = "sensor-name";
        String sensorCode = "sensor-code";
        if (!queryParams.containsKey(sensorName) || !queryParams.containsKey(sensorCode)){
            throw new IllegalArgumentException("ERROR PARAMETERS WHEN NEW SENSOR");
        }

        sensor.setCreator(user);
        sensor.setName(queryParams.get(sensorName));
        sensor.setCode(queryParams.get(sensorCode));

        String city = "city";
        String latitude = "latitude";
        String longitude = "longitude";
        String description = "description";
        if (queryParams.containsKey(latitude)){
            sensor.setLatitude(Double.valueOf(queryParams.get(latitude)));
        }
        if (queryParams.containsKey(longitude)){
            sensor.setLatitude(Double.valueOf(queryParams.get(longitude)));
        }
        if (queryParams.containsKey(city)){
            sensor.setCity(queryParams.get(city));
        }
        if (queryParams.containsKey(description)){
            sensor.setDescription(description);
        }

        Long id = crudService.saveSensor(sensor);
        logger.info("USER " + user + " NEW SENSOR " + id);
        return id;
    }

    @GetMapping("/sensor/delete")
    public Integer deleteSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        String user = commonService.getCurrentLoginUser(request);
        Long sensorId = Long.parseLong(queryParams.get("sensor-id"));
        if (sensorId <= 0){
            throw new IllegalArgumentException("ERROR PARAMETERS WHEN DELETE SENSOR");
        }
        logger.info("USER " + user + " DELETE TRACK " + sensorId);
        return crudService.deleteSensor(sensorId);
    }

    @GetMapping("/monitor")
    public Map monitorSensor(@RequestParam Map<String, String> queryParams){
        Long expId = Long.valueOf(queryParams.get("exp-id"));
        Map<String, Object> map;
        try {
            int response = crudService.changeSensorsMonitorStatusOfCurrentExperiment(expId);
            map = ServerReturnTool.serverSuccess(response);
        } catch (Exception e){
            map = ServerReturnTool.serverFailure(e.getMessage());
            logger.error("/crud/content", e);
        }
        return map;
    }

    @GetMapping("/isRecorder")
    public Map isRecorder(@RequestParam Map<String, String> queryParams){
        Long expId = Long.valueOf(queryParams.get("exp-id"));
        Map<String, Object> map;
        try {
            ExperimentInfo exp = crudService.findExp(expId);
            map = ServerReturnTool.serverSuccess(exp.getIsRecorder());
        } catch (Exception e){
            map = ServerReturnTool.serverFailure(e.getMessage());
            logger.error("/crud/isRecorder", e);
        }
        return map;
    }

    @GetMapping("/recorder")
    public Map recorderSensor(@RequestParam Map<String, String> queryParams){
        Long expId = Long.valueOf(queryParams.get("exp-id"));
        int isSave = Integer.parseInt(queryParams.get("is-save"));
        Map<String, Object> map;
        try {
            int response = crudService.changeSensorsRecorderStatusOfCurrentExperiment(expId, isSave);
            map = ServerReturnTool.serverSuccess(response);
        } catch (Exception e){
            map = ServerReturnTool.serverFailure(e.getMessage());
            logger.error("/crud/recorder", e);
        }
        return map;
    }
}
