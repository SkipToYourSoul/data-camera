package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.domain.common.ServerReturnTool;
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
    public Map getMonitorStatus(@RequestParam Map<String, String> queryParams){
        try {
            long expId = Long.parseLong(queryParams.get("exp-id"));
            return ServerReturnTool.serverSuccess(actionService.expCurrentStatus(expId).getValue());
        } catch (Exception e){
            logger.error("[/status]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }
}
