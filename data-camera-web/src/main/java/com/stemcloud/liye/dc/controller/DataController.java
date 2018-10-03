package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.common.ServerReturnTool;
import com.stemcloud.liye.dc.service.BaseInfoService;
import com.stemcloud.liye.dc.service.DataService;
import com.stemcloud.liye.dc.service.OssService;
import com.stemcloud.liye.dc.util.IpAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private final OssService ossService;

    @Autowired
    public DataController(BaseInfoService baseService, DataService dataService, OssService ossService) {
        this.baseService = baseService;
        this.dataService = dataService;
        this.ossService = ossService;
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
     * Map<Long, Map<String, List<ChartTimeSeries>>>
     *     sensor_id, (data_key, List<data_value>)
     */
    @GetMapping("/monitoring")
    Map monitor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        Map<String, Object> map;
        try {
            Long beginTime = System.currentTimeMillis();
            map = ServerReturnTool.serverSuccess(dataService.getRecentDataOfBoundSensors(Long.parseLong(queryParams.get("exp-id")), Long.parseLong(queryParams.get("timestamp"))));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
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
    Map getRecorderData(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        Map<String, Object> map;
        try{
            Long beginTime = System.currentTimeMillis();
            long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            map = ServerReturnTool.serverSuccess(dataService.getRecorderData(recorderId));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
        } catch (Exception e){
            map = ServerReturnTool.serverFailure(e.getMessage());
            logger.error("[/data/get-recorder-data]", e);
        }
        return map;
    }

    /**
     * 生成用户自定义的实验片段
     */
    @GetMapping("/user-new-recorder")
    Map newContent(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try {
            long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            String start = queryParams.get("start");
            String end = queryParams.get("end");
            String name = queryParams.get("name");
            String desc = queryParams.get("desc");
            Map result = ServerReturnTool.serverSuccess(dataService.generateUserContent(recorderId, start, end, name, desc));
            logger.info("Ip {} request ajax url {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString());
            return result;
        } catch (ParseException e) {
            logger.error("[/data/user-new-recorder]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    /**
     * 更新数据标注
     */
    @GetMapping("/user-data-mark")
    Map addDataMark(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try{
            long id = Long.parseLong(queryParams.get("data-id"));
            String mark = queryParams.get("data-mark");
            Map result =  ServerReturnTool.serverSuccess(dataService.updateDataMarker(id, mark));
            logger.info("Ip {} request ajax url {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString());
            return result;
        } catch (Exception e){
            logger.error("[/data/user-new-recorder]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @PostMapping("/file-upload")
    @ResponseBody
    Map uploadFile(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        try{
            long beginTime = System.currentTimeMillis();
            Map result = ServerReturnTool.serverSuccess(ossService.uploadFileToOss(request, queryParams.get("from")));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/data/user-new-recorder]", e);
            return ServerReturnTool.serverFailure(e.getCause().getMessage());
        }
    }

    @GetMapping("/search-hot-content")
    Map searchHotContent(@RequestParam String search, HttpServletRequest request) {
        try{
            long beginTime = System.currentTimeMillis();
            Map result = ServerReturnTool.serverSuccess(dataService.getSearchContent(search));
            logger.info("Ip {} request ajax url {}, cost {} ms", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString(), System.currentTimeMillis() - beginTime);
            return result;
        } catch (Exception e){
            logger.error("[/data/user-new-recorder]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }

    @PostMapping("/user-new-chart")
    Map userNewChart(@RequestParam Map<String, String> queryParams, HttpServletRequest request) {
        try {
            long sensorId = Long.parseLong(queryParams.get("sensor-select"));
            long recorderId = Long.parseLong(queryParams.get("recorder-id"));
            String x = queryParams.get("x-select");
            String y = queryParams.get("y-select");
            String name = queryParams.get("name-input");
            String desc = queryParams.get("desc-input");
            Map result = ServerReturnTool.serverSuccess(dataService.userNewChart(sensorId, recorderId, x, y, name, desc));
            logger.info("Ip {} request ajax url {}", IpAddressUtil.getClientIpAddress(request),
                    request.getRequestURL().toString());
            return result;
        } catch (Exception e) {
            logger.error("[/data/user-new-chart]", e);
            return ServerReturnTool.serverFailure(e.getMessage());
        }
    }
}