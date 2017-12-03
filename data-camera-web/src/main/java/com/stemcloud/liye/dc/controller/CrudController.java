package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.common.ServerReturnTool;
import com.stemcloud.liye.dc.domain.config.SensorRegister;
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
    public Map updateApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        AppInfo appInfo = null;
        try {
            String appId = "app-id";
            String appName = "app-name";
            String appDesc = "app-desc";
            String user = commonService.getCurrentLoginUser(request);
            if (!queryParams.containsKey(appId)){
                appInfo = new AppInfo();
            } else {
                Long id = Long.parseLong(queryParams.get("app-id"));
                appInfo = crudService.findApp(id);
                if (null == appInfo){
                    return ServerReturnTool.serverFailure("参数错误");
                }
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
        } catch (Exception e){
            logger.error("EDIT APP", e);
            return ServerReturnTool.serverFailure("后台数据错误");
        }

        return ServerReturnTool.serverSuccess(appInfo.getId());
    }

    /* delete app */
    @GetMapping("/app/delete")
    public Map deleteApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        if (!queryParams.containsKey("app-id") || Long.parseLong(queryParams.get("app-id")) < 0){
            return ServerReturnTool.serverFailure("参数错误");
        }
        try {
            Long id = Long.parseLong(queryParams.get("app-id"));
            String user = commonService.getCurrentLoginUser(request);
            logger.info("USER " + user + " DELETE APP " + id);
            crudService.deleteApp(id);
        } catch (Exception e){
            return ServerReturnTool.serverFailure("后台数据错误");
        }

        return ServerReturnTool.serverSuccess(Long.parseLong(queryParams.get("app-id")));
    }

    /* new or modify experiment */
    @PostMapping("/exp/update")
    public Map updateExp(@RequestParam Map<String, String> queryParams, @RequestParam(value = "exp-select", required = false) List<String> sensors, HttpServletRequest request){
        ExperimentInfo expInfo = null;
        try {
            String expId = "exp-id";
            String expName = "exp-name";
            String expDesc = "exp-desc";
            String user = commonService.getCurrentLoginUser(request);

            if (!queryParams.containsKey(expId)){
                expInfo = new ExperimentInfo();
            } else {
                Long id = Long.parseLong(queryParams.get("exp-id"));
                expInfo = crudService.findExp(id);
                if (null == expInfo){
                    return ServerReturnTool.serverFailure("参数错误");
                }
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
            if (null != sensors && sensors.size() > 0){
                for (String s: sensors){
                    long sensorId = Long.parseLong(s);
                    crudService.newTrackAndBoundSensor(newExpInfo, crudService.findSensor(sensorId));
                }
            }
            logger.info("USER " + user + " UPDATE EXP " + newExpInfo.getId());
        }catch (Exception e){
            logger.error("EDIT EXP", e);
            return ServerReturnTool.serverFailure("后台数据错误");
        }

        return ServerReturnTool.serverSuccess(expInfo.getId());
    }

    /* delete experiment */
    @GetMapping("/exp/delete")
    public Map deleteExp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        if (!queryParams.containsKey("exp-id") || Long.parseLong(queryParams.get("exp-id")) < 0){
            return ServerReturnTool.serverFailure("参数错误");
        }
        try {
            Long id = Long.parseLong(queryParams.get("exp-id"));
            String user = commonService.getCurrentLoginUser(request);
            logger.info("USER " + user + " DELETE EXP " + id);
            crudService.deleteExp(id);
        } catch (Exception e){
            return ServerReturnTool.serverFailure("后台数据错误");
        }

        return ServerReturnTool.serverSuccess(Long.parseLong(queryParams.get("exp-id")));
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

    @PostMapping("/sensor/update")
    public Map newSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        SensorInfo sensor = null;
        try {
            String user = commonService.getCurrentLoginUser(request);
            String sensorId = "sensor-id";
            String sensorName = "sensor-name";
            String sensorCode = "sensor-code";
            String sensorDesc = "sensor-desc";
            if (!queryParams.containsKey(sensorId)){
                sensor = new SensorInfo();
                SensorRegister sensorRegister = crudService.findRegister(queryParams.get(sensorCode));
                if (sensorRegister == null){
                    return ServerReturnTool.serverFailure("传感器编号错误，系统未能识别该编号");
                } else if (sensorRegister.getIsRegistered() == 1){
                    return ServerReturnTool.serverFailure("该编号已被绑定，无法再次新建");
                }
                sensor.setCode(queryParams.get(sensorCode));
                sensor.setSensorConfig(sensorRegister.getSensorConfig());
                crudService.registerSensor(1, queryParams.get(sensorCode));
            } else {
                Long id = Long.parseLong(queryParams.get(sensorId));
                sensor = crudService.findSensor(id);
                if (sensor == null){
                    return ServerReturnTool.serverFailure("参数错误");
                }
            }

            sensor.setCreator(user);
            sensor.setName(queryParams.get(sensorName));
            sensor.setDescription(queryParams.get(sensorDesc));

            String city = "city";
            String latitude = "latitude";
            String longitude = "longitude";
            if (queryParams.containsKey(latitude)){
                sensor.setLatitude(Double.valueOf(queryParams.get(latitude)));
            }
            if (queryParams.containsKey(longitude)){
                sensor.setLatitude(Double.valueOf(queryParams.get(longitude)));
            }
            if (queryParams.containsKey(city)){
                sensor.setCity(queryParams.get(city));
            }

            Long id = crudService.saveSensor(sensor);
            logger.info("USER " + user + " UPDATE SENSOR " + id);
        } catch (Exception e){
            logger.error("EDIT SENSOR", e);
            return ServerReturnTool.serverFailure("后台数据错误");
        }

        return ServerReturnTool.serverSuccess(sensor.getId());
    }

    @GetMapping("/sensor/delete")
    public Map deleteSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        if (!queryParams.containsKey("sensor-id") || Long.parseLong(queryParams.get("sensor-id")) < 0){
            return ServerReturnTool.serverFailure("参数错误");
        }
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long sensorId = Long.parseLong(queryParams.get("sensor-id"));
            logger.info("USER " + user + " DELETE TRACK " + sensorId);
            crudService.deleteSensor(sensorId, queryParams.get("sensor-code"));
        } catch (Exception e){
            return ServerReturnTool.serverFailure("后台数据错误");
        }
        return ServerReturnTool.serverSuccess(Long.parseLong(queryParams.get("sensor-id")));
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

    @PostMapping("/content/name")
    public Map modifyContentName(@RequestParam Map<String, String> queryParams){
        String result = "";
        try {
            long contentId = Long.parseLong(queryParams.get("pk"));
            String name = queryParams.get("value");
            crudService.updateContentName(contentId, name);
            result = "内容名称变更为：" + name;
        } catch (Exception e){
            return ServerReturnTool.serverFailure("后台数据错误");
        }

        return ServerReturnTool.serverSuccess(result);
    }
}
