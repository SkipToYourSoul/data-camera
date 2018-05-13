package com.stemcloud.liye.dc.controller;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.base.TrackInfo;
import com.stemcloud.liye.dc.domain.data.ContentInfo;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.service.BaseInfoService;
import com.stemcloud.liye.dc.service.CommonService;
import com.stemcloud.liye.dc.service.CrudService;
import com.stemcloud.liye.dc.util.IpAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  controller of view (*.html)
 * @author liye on 2017/11/6
 */
@Controller
public class ViewController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonService commonService;
    private final BaseInfoService baseInfoService;
    private final CrudService crudService;

    @Autowired
    public ViewController(CommonService commonService, BaseInfoService baseInfoService, CrudService crudService) {
        this.commonService = commonService;
        this.baseInfoService = baseInfoService;
        this.crudService = crudService;
    }

    /**
     * 主页
     */
    @GetMapping("/index")
    public String index(Model model, HttpServletRequest request) {
        logger.info("Ip {} request view url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());
        model.addAttribute("inIndex", true);
        return "index";
    }

    /**
     * 场景页
     */
    @GetMapping("/app")
    public String app(@RequestParam(value = "id", required = false) Long id, Model model,
                      HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Ip {} request view url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());

        // --- add base information
        String user = commonService.getCurrentLoginUser(request);
        Map<Long, AppInfo> apps = baseInfoService.getOnlineApps(user);
        model.addAttribute("apps", apps);
        model.addAttribute("inApp", true);

        if (id != null) {
            // --- can't visit app if app not belong current user
            if (!baseInfoService.isAppBelongUser(id, user)){
                logger.warn("[/app], the user {} has not the app {}, redirect to /index", user, id);
                response.sendRedirect(request.getContextPath() + "/index");
                return "index";
            }

            // --- EXP: get experiments of the app, get bound sensors, get monitor, recorder sensors
            Map<Long, ExperimentInfo> experiments = baseInfoService.getOnlineExpOfApp(id);
            Map<Long, List<SensorInfo>> boundSensors = new HashMap<Long, List<SensorInfo>>(experiments.size());
            Map<Long, Integer> isExperimentMonitor = new HashMap<Long, Integer>(experiments.size());
            Map<Long, Integer> isExperimentRecorder = new HashMap<Long, Integer>(experiments.size());
            Map<Long, Date> expRecorderTime = new HashMap<Long, Date>(experiments.size());
            Map<Long, TrackInfo> tracks = new HashMap<Long, TrackInfo>(16);

            for (Map.Entry<Long, ExperimentInfo> entry : experiments.entrySet()){
                long expId = entry.getKey();
                ExperimentInfo exp = entry.getValue();
                isExperimentMonitor.put(expId, exp.getIsMonitor());
                isExperimentRecorder.put(expId, exp.getIsRecorder());
                if (exp.getIsRecorder() == 1){
                    expRecorderTime.put(expId, baseInfoService.getRecorderInfoOfExp(expId).getStartTime());
                }

                Set<TrackInfo> newTracks = new HashSet<TrackInfo>();
                for (TrackInfo track: exp.getTrackInfoList()){
                    if (track.getIsDeleted() == 0){
                        tracks.put(track.getId(), track);
                        TrackInfo newTrack = new TrackInfo();
                        newTrack.setId(track.getId());
                        newTrack.setSensor(track.getSensor());
                        newTrack.setType(track.getType());
                        newTracks.add(newTrack);

                        // add bound sensors
                        if (track.getSensor() != null){
                            List<SensorInfo> expBoundSensors = new ArrayList<SensorInfo>();
                            if (boundSensors.containsKey(expId)){
                                expBoundSensors = boundSensors.get(expId);
                            }
                            expBoundSensors.add(track.getSensor());
                            boundSensors.put(expId, expBoundSensors);
                        }
                    }
                }
                exp.setTrackInfoList(newTracks);
            }

            // --- ADD MODEL INFO
            model.addAttribute("experiments", experiments);
            model.addAttribute("isExperimentMonitor", isExperimentMonitor);
            model.addAttribute("isExperimentRecorder", isExperimentRecorder);
            model.addAttribute("expRecorderTime", expRecorderTime);

            // --- APP
            model.addAttribute("app", crudService.findApp(id));

            // --- TRACK
            model.addAttribute("tracks", tracks);

            // --- SENSOR: get user's sensor of this app and available sensors
            Map<Long, SensorInfo> availableSensor = baseInfoService.getAvailableSensorOfCurrentUser(user);
            model.addAttribute("freeSensors", availableSensor);
            model.addAttribute("boundSensors", boundSensors);
            model.addAttribute("sensors", baseInfoService.getOnlineSensor(user));

            // --- RECORDER:
            model.addAttribute("recorders", baseInfoService.getRecordersOfApp(id));
        }

        return "app";
    }

    /**
     * 设备页
     */
    @GetMapping("/device")
    public String device(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Ip {} request view url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());

        // --- get the devices of user
        String currentUser = commonService.getCurrentLoginUser(request);
        List<SensorInfo> sensors = baseInfoService.getOnlineSensor(currentUser);
        model.addAttribute("sensors", sensors);
        model.addAttribute("inDevice", true);

        return "device";
    }

    /**
     * 内容页
     */
    @GetMapping("/content")
    public String content(@RequestParam(value = "id", required = false) Long id,
                          Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Ip {} request view url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());
        // --- get login user name
        String currentUser = commonService.getCurrentLoginUser(request);
        model.addAttribute("inContent", true);

        if (id == null){
            // 内容浏览页
            List<ContentInfo> userContent = crudService.selectUserContent(currentUser);
            List<ContentInfo> hotContent = crudService.selectHotContent();

            model.addAttribute("userContent", userContent);
            model.addAttribute("hotContent", hotContent);
        } else {
            // 内容详情页
            if (!baseInfoService.isContentCanVisit(id, currentUser)){
                logger.warn("[/content], the user {} can not visit content {}, redirect to /index", currentUser, id);
                response.sendRedirect(request.getContextPath() + "/index");
                return "index";
            }
            ContentInfo currentContent = crudService.findContent(id);
            List<ContentInfo> userHotContent = crudService.selectUserHotContent(currentContent.getOwner());
            userHotContent.remove(currentContent);

            model.addAttribute("currentContent", currentContent);
            model.addAttribute("userHotContent", userHotContent);

            Map<Long, RecorderInfo> map = new HashMap<Long, RecorderInfo>(1);
            map.put(currentContent.getRecorderInfo().getId(), currentContent.getRecorderInfo());
            model.addAttribute("recorders", map);
        }

        return "content";
    }

    /**
     * 热门内容页
     */
    @GetMapping("/hot-content")
    public String hotContent(Model model, HttpServletRequest request) {
        logger.info("Ip {} request view url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());
        model.addAttribute("inContent", true);
        String currentUser = commonService.getCurrentLoginUser(request);
        List<ContentInfo> userContent = crudService.selectUserContent(currentUser);
        List<ContentInfo> hotContent = crudService.selectHotContent();

        model.addAttribute("userContent", userContent);
        model.addAttribute("hotContent", hotContent);
        return "hot-content";
    }

    /**
     * 分享页
     */
    @GetMapping("/share")
    public String shareContent(@RequestParam(value = "rid") Long rid, Model model, HttpServletRequest request) throws IOException {
        logger.info("Ip {} request view url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());
        RecorderInfo recorderInfo = crudService.findRecorder(rid);
        model.addAttribute("recorder", recorderInfo);

        return "share";
    }

    @GetMapping("/admin")
    public String admin(Model model, HttpServletRequest request){
        String currentUser = commonService.getCurrentLoginUser(request);
        logger.info("Admin user {} request the admin page.", currentUser);
        return "admin";
    }

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        logger.info("Ip {} request view url {}", IpAddressUtil.getClientIpAddress(request), request.getRequestURL().toString());
        return "test";
    }

    @GetMapping("/denied")
    public String denied() {
        return "denied";
    }

    @GetMapping("/exception")
    public String exception(){
        return "exception";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request){
        logger.info("Ip {} request login page from url {}", IpAddressUtil.getClientIpAddress(request), request.getHeader("Referer"));
        return "login";
    }
}