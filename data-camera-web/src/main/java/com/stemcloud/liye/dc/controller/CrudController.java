package com.stemcloud.liye.dc.controller;

import com.google.gson.Gson;
import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.common.ServerReturnTool;
import com.stemcloud.liye.dc.domain.config.SensorRegister;
import com.stemcloud.liye.dc.domain.data.ContentInfo;
import com.stemcloud.liye.dc.service.BaseInfoService;
import com.stemcloud.liye.dc.service.CommonService;
import com.stemcloud.liye.dc.service.CrudService;
import com.stemcloud.liye.dc.util.IpAddressUtil;
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

    /**
     * 新建场景
     */
    @PostMapping("/app/new")
    public Map newApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            AppInfo newApp = crudService.saveApp(user, queryParams.get("app-name"), queryParams.get("app-desc"));
            Map result = ServerReturnTool.serverSuccess(newApp);
            logger.info("Ip {} request ajax url {}, user {} new app {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, newApp.getId());
            return result;
        } catch (Exception e){
            logger.error("[/crud/app/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 编辑场景
     */
    @PostMapping("/app/update")
    public Map updateApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            AppInfo newApp = crudService.updateApp(Long.valueOf(queryParams.get("app-id")), queryParams.get("app-name"), queryParams.get("app-desc"));
            Map result = ServerReturnTool.serverSuccess(newApp);
            logger.info("Ip {} request ajax url {}, user {} update app {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, newApp.getId());
            return result;
        } catch (Exception e){
            logger.error("[/crud/app/update]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除场景
     */
    @GetMapping("/app/delete")
    public Map deleteApp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            Long id = Long.parseLong(queryParams.get("app-id"));
            String user = commonService.getCurrentLoginUser(request);
            crudService.deleteApp(id);
            logger.info("Ip {} request ajax url {}, user {} delete app {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, id);
            return ServerReturnTool.serverSuccess(id);
        } catch (Exception e){
            logger.error("[/crud/app/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 新建传感器组
     */
    @PostMapping("/exp/new")
    public Map newExp(@RequestParam Map<String, String> queryParams,
                      @RequestParam(value = "exp-select", required = false) List<String> sensors, HttpServletRequest request){
        try {

            String user = commonService.getCurrentLoginUser(request);
            ExperimentInfo newExpInfo = crudService.saveExp(Long.valueOf(queryParams.get("app-id")),
                    queryParams.get("exp-name"), queryParams.get("exp-desc"), sensors);
            logger.info("Ip {} request ajax url {}, user {} new exp {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, newExpInfo.getId());
            return ServerReturnTool.serverSuccess(newExpInfo);
        } catch (Exception e){
            logger.error("[/crud/exp/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 编辑传感器组
     */
    @PostMapping("/exp/update")
    public Map updateExp(@RequestParam Map<String, String> queryParams, @RequestParam(value = "exp-select", required = false) List<String> sensors, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            ExperimentInfo newExpInfo = crudService.updateExp(Long.valueOf(queryParams.get("exp-id")),
                    queryParams.get("exp-name"), queryParams.get("exp-desc"), sensors);
            logger.info("Ip {} request ajax url {}, user {} update exp {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, newExpInfo.getId());
            return ServerReturnTool.serverSuccess(newExpInfo);
        }catch (Exception e){
            logger.error("[/crud/exp/update]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除传感器组
     */
    @GetMapping("/exp/delete")
    public Map deleteExp(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            Long id = Long.parseLong(queryParams.get("exp-id"));
            String user = commonService.getCurrentLoginUser(request);
            crudService.deleteExp(id);
            logger.info("Ip {} request ajax url {}, user {} delete exp {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, id);
            return ServerReturnTool.serverSuccess(id);
        } catch (Exception e){
            logger.error("[/crud/exp/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除轨迹
     */
    @GetMapping("/track/delete")
    public Map deleteTrack(@RequestParam List<String> ids, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            for (String id : ids){
                crudService.deleteTrack(Long.parseLong(id));
            }
            logger.info("Ip {} request ajax url {}, user {} delete track {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, new Gson().toJson(ids));
            return ServerReturnTool.serverSuccess(ids.size());
        } catch (Exception e){
            logger.error("[/crud/track/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 绑定和解绑传感器
     */
    @PostMapping("/bound/toggle")
    public Map boundToggle(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            Map<String, String> result = new HashMap<String, String>(16);
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
            logger.info("Ip {} request ajax url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());
            return ServerReturnTool.serverSuccess(result);
        } catch (Exception e){
            logger.error("[/crud/bound/toggle]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 新增设备
     */
    @PostMapping("/sensor/new")
    public Map newSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);

            // 查看注册表是否已注册该设备
            SensorRegister sensorRegister = crudService.findRegister(queryParams.get("sensor-code"));
            if (sensorRegister == null){
                return ServerReturnTool.serverFailure("传感器编号错误，系统未能识别该编号");
            } else if (sensorRegister.getIsRegistered() == 1){
                return ServerReturnTool.serverFailure("该编号已被其他用户绑定，无法新增");
            }
            SensorInfo newSensor = crudService.saveSensor(user, queryParams.get("sensor-code"), sensorRegister.getSensorConfig(),
                    queryParams.get("device-img"), queryParams.get("sensor-name"), queryParams.get("sensor-desc"));
            logger.info("Ip {} request ajax url {}, user {} new sensor {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, newSensor.getId());
            return ServerReturnTool.serverSuccess(newSensor);
        } catch (Exception e){
            logger.error("[/crud/sensor/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 更新设备信息
     */
    @PostMapping("/sensor/update")
    public Map updateSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            SensorInfo newSensor = crudService.updateSensor(Long.valueOf(queryParams.get("sensor-id")), queryParams.get("device-img"),
                    queryParams.get("sensor-name"), queryParams.get("sensor-desc"));
            logger.info("Ip {} request ajax url {}, user {} update sensor {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, newSensor.getId());
            return ServerReturnTool.serverSuccess(newSensor);
        } catch (Exception e){
            logger.error("[/crud/sensor/update]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除设备
     */
    @GetMapping("/sensor/delete")
    public Map deleteSensor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long sensorId = Long.parseLong(queryParams.get("sensor-id"));
            crudService.deleteSensor(sensorId, queryParams.get("sensor-code"));
            logger.info("Ip {} request ajax url {}, user {} delete sensor {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, sensorId);
            return ServerReturnTool.serverSuccess(sensorId);
        } catch (Exception e){
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 修改数据片段名称
     */
    @PostMapping("/recorder/name")
    public Map modifySegmentName(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            long recorderId = Long.parseLong(queryParams.get("pk"));
            String name = queryParams.get("value");
            crudService.updateRecorderName(recorderId, name);
            logger.info("Ip {} request ajax url {}, user {} update recorder name", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user);
            return ServerReturnTool.serverSuccess(name);
        } catch (Exception e){
            logger.error("[/crud/recorder/name]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 修改数据片段描述
     */
    @GetMapping("/recorder/desc")
    public Map modifySegmentDesc(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            long recorderId = Long.parseLong(queryParams.get("id"));
            String title = queryParams.get("title");
            String desc = queryParams.get("desc");
            crudService.updateRecorderDescription(recorderId, title, desc);
            logger.info("Ip {} request ajax url {}, user {} update recorder description", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user);
            return ServerReturnTool.serverSuccess(desc);
        } catch (Exception e){
            logger.error("[/crud/recorder/desc]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除数据片段
     */
    @GetMapping("/recorder/delete")
    public Map deleteRecorder(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            crudService.deleteAllRecorder(recorderId);
            logger.info("Ip {} request ajax url {}, user {} delete recorder", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user);
            return ServerReturnTool.serverSuccess(Long.parseLong(queryParams.get("recorder-id")));
        } catch (Exception e){
            logger.error("[/crud/recorder/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 发布内容
     */
    @PostMapping("/content/new")
    public Map publishContent(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            String user = commonService.getCurrentLoginUser(request);
            Long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            String name = queryParams.get("content-name");
            String desc = queryParams.get("content-desc");
            String category = queryParams.get("content-category-select");
            String tag = queryParams.get("tags");
            String img = queryParams.get("share-img");
            int isShared = Integer.parseInt(queryParams.get("content-private-select"));
            ContentInfo content = crudService.saveContent(user, name, desc, category, tag, isShared, recorderId, img);
            logger.info("Ip {} request ajax url {}, user {} new content {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, content.getId());
            return ServerReturnTool.serverSuccess(content);
        } catch (Exception e){
            logger.error("[/content/new]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 删除内容
     */
    @GetMapping("/content/delete")
    public Map deleteContent(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            Long id = Long.parseLong(queryParams.get("content-id"));
            String user = commonService.getCurrentLoginUser(request);
            crudService.deleteContent(id);
            logger.info("Ip {} request ajax url {}, user {} delete content {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), user, id);
            return ServerReturnTool.serverSuccess(id);
        } catch (Exception e){
            logger.error("[/crud/content/delete]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }
}
