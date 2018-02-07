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
     * @param model
     * @return
     */
    @GetMapping("/index")
    public String index(Model model) {
        logger.info("[/index], in index page");
        model.addAttribute("inIndex", true);
        return "index";
    }

    /**
     * 应用页
     * @param id
     * @param model
     * @param request http request
     * @param response http response
     * @return
     * @throws IOException
     */
    @GetMapping("/app")
    public String app(@RequestParam(value = "id", required = false) Long id, Model model,
                      HttpServletRequest request, HttpServletResponse response) throws IOException {
        // --- login before visit
        String user = commonService.getCurrentLoginUser(request);
        if (user == null) {
            logger.warn("[/app], no login user, redirect to /login");
            response.sendRedirect(request.getContextPath() + "/login");
            return "login";
        }

        // --- add base information
        Map<Long, AppInfo> apps = baseInfoService.getOnlineApps(user);
        model.addAttribute("apps", apps);
        model.addAttribute("inApp", true);

        if (id == null){
            // --- id is null, in app management page
            logger.info("[/app], in app manage page");
        } else {
            logger.info("[/app], in app detail page");
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
            Map<Long, List<RecorderInfo>> recorders = baseInfoService.getAllRecorders(apps);
            model.addAttribute("recorders", recorders);
        }

        return "app";
    }

    @GetMapping("/device")
    public String device(Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // --- get login user name
        String currentUser = commonService.getCurrentLoginUser(request);
        if (currentUser == null) {
            logger.warn("[/device], no login user, redirect to /login");
            response.sendRedirect(request.getContextPath() + "/login");
            return "login";
        }

        // --- get the devices of user
        List<SensorInfo> sensors = baseInfoService.getOnlineSensor(currentUser);
        model.addAttribute("sensors", sensors);
        model.addAttribute("inDevice", true);

        return "device";
    }

    @GetMapping("/content")
    public String content(@RequestParam(value = "id", required = false) Long id,
                          Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // --- get login user name
        String currentUser = commonService.getCurrentLoginUser(request);
        if (currentUser == null) {
            logger.warn("[/content], no login user, redirect to /login");
            response.sendRedirect(request.getContextPath() + "/login");
            return "login";
        }

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
        }

        return "content";
    }

    @GetMapping("/share")
    public String shareContent(@RequestParam(value = "rid", required = false) Long rid,
                               Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String currentUser = commonService.getCurrentLoginUser(request);
        if (currentUser == null){
            logger.warn("[/share], no login user, redirect to /login");
            response.sendRedirect(request.getContextPath() + "/login");
            return "login";
        }
        if (rid == null){
            logger.warn("[/share], unsupport url");
            response.sendRedirect(request.getContextPath() + "/index");
            return "index";
        }
        RecorderInfo recorderInfo = crudService.findRecorder(rid);
        model.addAttribute("recorder", recorderInfo);
        model.addAttribute("inContent", true);

        return "share";
    }

    @GetMapping("/denied")
    public String denied(HttpServletRequest request) {
        logger.warn("denied: {}", request.getRequestURL().toString());
        return "denied";
    }

    @GetMapping("/exception")
    public String exception(){
        logger.warn("exception");
        return "exception";
    }

    @GetMapping("/login")
    public String login(){
        logger.info("In login.html");
        return "login";
    }  
}