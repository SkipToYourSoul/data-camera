package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.common.ServerReturnTool;
import com.stemcloud.liye.dc.service.BaseInfoService;
import com.stemcloud.liye.dc.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  asyn ajax request
 * @author liye on 2017/11/6
 */
@RestController
@RequestMapping("/data")
public class DataController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BaseInfoService baseService;
    private final DataService dataService;

    @Autowired
    public DataController(BaseInfoService baseService, DataService dataService) {
        this.baseService = baseService;
        this.dataService = dataService;
    }

    @GetMapping("/app")
    Map<Long, AppInfo> app(){
        return baseService.getOnlineApps("root");
    }

    @GetMapping("/sensor")
    List<SensorInfo> sensor(){
        return baseService.getOnlineSensor("root");
    }

    @GetMapping("/experiment")
    List<ExperimentInfo> experiment(){
        return baseService.getOnlineExp();
    }

    @GetMapping("/track")
    List<TrackInfo> track(){
        return baseService.getOnlineTrack();
    }

    /**
     * 获取更新的监控数据
     *
     * Map<Long, Map<String, List<ChartTimeSeries>>>
     *     sensor_id, (data_key, List<data_value>)
     * @param queryParams
     * @param request
     * @return
     */
    @GetMapping("/monitoring")
    Map monitor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        Map<String, Object> map;
        try {
            Long beginTime = System.currentTimeMillis();
            map = ServerReturnTool.serverSuccess(dataService.getRecentDataOfBoundSensors(Long.parseLong(queryParams.get("exp-id")), Long.parseLong(queryParams.get("timestamp"))));
            Long endTime = System.currentTimeMillis();
            logger.debug("[/data/monitoring] request data in {} ms.", (endTime - beginTime));
        } catch (Exception e){
            map = ServerReturnTool.serverFailure(e.getMessage());
            logger.error("[/data/monitoring]", e);
        }

        return map;
    }

    /**
     * 获取实验片段数据
     *  CHART: Map<Long, Map<String, List<ChartTimeSeries>>>
     *  VIDEO: Map<Long, Video>
     * @param queryParams recorderId
     * @return Map
     */
    @GetMapping("/get-recorder-data")
    Map getRecorderData(@RequestParam Map<String, String> queryParams){
        Map<String, Object> map;
        try{
            Long beginTime = System.currentTimeMillis();
            long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            map = ServerReturnTool.serverSuccess(dataService.getRecorderData(recorderId));
            Long endTime = System.currentTimeMillis();
            logger.info("[/data/get-recorder-data] request recorder data, cost={} ms.", (endTime - beginTime));
        } catch (Exception e){
            map = ServerReturnTool.serverFailure(e.getMessage());
            logger.error("[/data/get-recorder-data]", e);
        }
        return map;
    }

    /**
     * 生成用户自定义的实验片段
     *
     * @param queryParams
     * @return
     */
    @GetMapping("/user-new-recorder")
    Map newContent(@RequestParam Map<String, String> queryParams){
        try {
            long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            String start = queryParams.get("start");
            String end = queryParams.get("end");
            logger.info("[/data/user-new-recorder], id={}, start={}, end={}", recorderId, start, end);
            return ServerReturnTool.serverSuccess(dataService.generateUserContent(recorderId, start, end));
        } catch (ParseException e) {
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @GetMapping("/user-data-mark")
    Map addDataMark(@RequestParam Map<String, String> queryParams){
        try{
            long id = Long.parseLong(queryParams.get("data-id"));
            String mark = queryParams.get("data-mark");
            return ServerReturnTool.serverSuccess(dataService.updateDataMarker(id, mark));
        } catch (Exception e){
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }
}