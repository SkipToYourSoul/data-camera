package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.common.ServerReturnTool;
import com.stemcloud.liye.dc.service.ActionService;
import com.stemcloud.liye.dc.util.IpAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  控制实验的监控、录制操作
 * @author liye on 2018/1/11
 */
@RestController
@RequestMapping("/action")
public class ActionController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ActionService actionService;

    @Autowired
    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     *  从服务器获取当前实验的状态
     */
    @GetMapping("/status")
    public Map getStatus(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            long beginTime = System.currentTimeMillis();
            long expId = Long.parseLong(queryParams.get("exp-id"));
            Map result = ServerReturnTool.serverSuccess(actionService.expCurrentStatus(expId).getValue());
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/action/status]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     *  改变实验的监控状态
     */
    @GetMapping("/monitor")
    public Map expMonitor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            long beginTime = System.currentTimeMillis();
            long expId = Long.parseLong(queryParams.get("exp-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            Map result = ServerReturnTool.serverSuccess(actionService.changeMonitorState(expId, action, isSave, dataTime, name, desc));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/action/monitor]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     *  改变实验的录制状态
     */
    @GetMapping("/record")
    public Map expRecord(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            long beginTime = System.currentTimeMillis();
            long expId = Long.parseLong(queryParams.get("exp-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            Map result =  ServerReturnTool.serverSuccess(actionService.changeRecorderState(expId, action, isSave, dataTime, name, desc));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/record]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     *  从服务器获取当前场景下所有的实验的状态
     */
    @GetMapping("/status/all")
    public Map getAllStatus(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            long beginTime = System.currentTimeMillis();
            long appId = Long.parseLong(queryParams.get("app-id"));
            Map result = ServerReturnTool.serverSuccess(actionService.expAllStatus(appId).getValue());
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/status/all]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     *  全局监控
     */
    @GetMapping("/monitor/all")
    public Map expMonitorAll(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            long beginTime = System.currentTimeMillis();
            Long appId = Long.valueOf(queryParams.get("app-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            Map result = ServerReturnTool.serverSuccess(actionService.allMonitor(appId, action, isSave, dataTime, name, desc));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/action/monitor/all]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     *  全局录制
     */
    @GetMapping("/record/all")
    public Map expRecordAll(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            long beginTime = System.currentTimeMillis();
            Long appId = Long.valueOf(queryParams.get("app-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            Map result = ServerReturnTool.serverSuccess(actionService.allRecorder(appId, action, isSave, dataTime, name, desc));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/action/record/all]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }
}
