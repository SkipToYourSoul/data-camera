package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.common.ServerReturnTool;
import com.stemcloud.liye.dc.service.ActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping("/status")
    public Map getStatus(@RequestParam Map<String, String> queryParams){
        try {
            long expId = Long.parseLong(queryParams.get("exp-id"));
            return ServerReturnTool.serverSuccess(actionService.expCurrentStatus(expId).getValue());
        } catch (Exception e){
            logger.error("[/status]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @GetMapping("/monitor")
    public Map expMonitor(@RequestParam Map<String, String> queryParams){
        try {
            long expId = Long.parseLong(queryParams.get("exp-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            return ServerReturnTool.serverSuccess(actionService.changeMonitorState(expId, action, isSave, dataTime, name, desc));
        } catch (Exception e){
            logger.error("[/monitor]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @GetMapping("/record")
    public Map expRecord(@RequestParam Map<String, String> queryParams){
        try {
            long expId = Long.parseLong(queryParams.get("exp-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            return ServerReturnTool.serverSuccess(actionService.changeRecorderState(expId, action, isSave, dataTime, name, desc));
        } catch (Exception e){
            logger.error("[/record]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @GetMapping("/status/all")
    public Map getAllStatus(@RequestParam Map<String, String> queryParams){
        try {
            long appId = Long.parseLong(queryParams.get("app-id"));
            return ServerReturnTool.serverSuccess(actionService.expAllStatus(appId).getValue());
        } catch (Exception e){
            logger.error("[/status/all]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @GetMapping("/monitor/all")
    public Map expMonitorAll(@RequestParam Map<String, String> queryParams){
        try {
            Long appId = Long.valueOf(queryParams.get("app-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            return ServerReturnTool.serverSuccess(actionService.allMonitor(appId, action, isSave, dataTime, name, desc));
        } catch (Exception e){
            logger.error("[/monitor/all]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @GetMapping("/record/all")
    public Map expRecordAll(@RequestParam Map<String, String> queryParams){
        try {
            Long appId = Long.valueOf(queryParams.get("app-id"));
            int action = Integer.parseInt(queryParams.get("action"));
            int isSave = Integer.parseInt(queryParams.get("isSave"));
            long dataTime = Long.parseLong(queryParams.get("data-time"));
            String name = queryParams.get("data-name");
            String desc = queryParams.get("data-desc");
            return ServerReturnTool.serverSuccess(actionService.allRecorder(appId, action, isSave, dataTime, name, desc));
        } catch (Exception e){
            logger.error("[/record/all]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }
}
