package com.stemcloud.liye.project.controller;

import com.stemcloud.liye.project.domain.base.AppInfo;
import com.stemcloud.liye.project.domain.base.ExperimentInfo;
import com.stemcloud.liye.project.domain.base.SensorInfo;
import com.stemcloud.liye.project.domain.base.TrackInfo;
import com.stemcloud.liye.project.service.BaseInfoService;
import com.stemcloud.liye.project.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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
    private final BaseInfoService baseService;
    private final DataService dataService;

    @Autowired
    public DataController(BaseInfoService baseService, DataService dataService) {
        this.baseService = baseService;
        this.dataService = dataService;
    }

    @GetMapping("/app")
    List<AppInfo> app(){
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

    @GetMapping("/monitor")
    String monitor(@RequestParam Map<String, String> queryParams, HttpServletRequest request){
        for (Map.Entry<String, String> entry: queryParams.entrySet()){
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        dataService.getRecentDataOfBoundSensors(Long.parseLong(queryParams.get("exp-id")), Long.parseLong(queryParams.get("timestamp")));

        return "success";
    }
}
