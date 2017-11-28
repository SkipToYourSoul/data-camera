package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.view.ServerReturnTool;
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
import java.util.HashMap;
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
     * Map<Long, Map<String, List<ChartTimeSeries>>>
     *     sensor_id, (data_key, List<data_value>)
     * @param queryParams
     * @param request
     * @return
     */
    @GetMapping("/monitor")
    Map monitor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        Long beginTime = System.currentTimeMillis();
        Map result = dataService.getRecentDataOfBoundSensors(Long.parseLong(queryParams.get("exp-id")), Long.parseLong(queryParams.get("timestamp")));
        Long endTime = System.currentTimeMillis();

        logger.info("request monitor data in {} ms.", (endTime - beginTime));
        return result;
    }

    /**
     * Map<Long, Map<Long, Map<String, List<ChartTimeSeries>>>>
     *      content_id, (sensor_id, (data_key, List<data_value>))
     * @param queryParams
     * @return
     */
    @GetMapping("/content")
    Map content(@RequestParam Map<String, String> queryParams){
        Map<String, Object> map;
        try{
            Long beginTime = System.currentTimeMillis();
            long expId = Long.parseLong(queryParams.get("exp-id"));
            map = ServerReturnTool.serverSuccess(dataService.getContentDataOfExperiment(expId));
            Long endTime = System.currentTimeMillis();
            logger.info("request content data in {} ms.", (endTime - beginTime));
        } catch (Exception e){
            map = ServerReturnTool.serverFailure(e.getMessage());
            logger.error("/data/content", e);
        }
        return map;
    }
}
