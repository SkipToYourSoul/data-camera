package com.stemcloud.liye.dc.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stemcloud.liye.dc.dao.data.ContentRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.dao.data.ValueDataRepository;
import com.stemcloud.liye.dc.domain.data.ContentInfo;
import com.stemcloud.liye.dc.domain.data.RecorderDevices;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.data.ValueData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  后台管理controller
 * @author liye on 2018/8/19
 */
@RestController
@RequestMapping("/manage")
public class AdminController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Gson gson = new Gson();

    private final RecorderRepository recorderRepository;
    private final ContentRepository contentRepository;
    private final ValueDataRepository valueDataRepository;

    public AdminController(RecorderRepository recorderRepository, ContentRepository contentRepository, ValueDataRepository valueDataRepository) {
        this.recorderRepository = recorderRepository;
        this.contentRepository = contentRepository;
        this.valueDataRepository = valueDataRepository;
    }

    @GetMapping("/deleteValueData")
    public void deleteValueData() {
        Set<Long> onlineRecorder = new HashSet<Long>();

        // 找出当前在线的content对应的recorderId
        List<ContentInfo> contentInfos = contentRepository.findByIsDeleted(0);
        for (ContentInfo c : contentInfos) {
            onlineRecorder.add(c.getRecorderInfo().getId());
        }
        logger.info("online content's recorder ids is {}", gson.toJson(onlineRecorder));

        // 找出当前在线的recorder
        List<RecorderInfo> recorderInfos = recorderRepository.findByIsDeletedOrderByIdDesc(0);
        for (RecorderInfo r : recorderInfos) {
            onlineRecorder.add(r.getId());
        }
        logger.info("online recorder ids is {}", gson.toJson(onlineRecorder));

        List<Long> onlineValueIds = new ArrayList<Long>();
        // 逐一处理，找出online的value
        for (Long id : onlineRecorder) {
            logger.info("handle recorder {}", id);
            RecorderInfo r = recorderRepository.findOne(id);
            List<RecorderDevices> devices = gson.fromJson(r.getDevices(), new TypeToken<ArrayList<RecorderDevices>>(){}.getType());
            Date startTime = r.getStartTime();
            Date endTime = r.getEndTime();
            for (RecorderDevices d : devices) {
                long sensorId = d.getSensor();
                long track = d.getTrack();
                List<ValueData> values = valueDataRepository.findBySensorIdAndTrackIdAndCreateTimeGreaterThanEqualAndCreateTimeLessThanEqualOrderByCreateTime(sensorId, track, startTime, endTime);
                for (ValueData v : values) {
                    onlineValueIds.add(v.getId());
                }
            }
        }
        logger.info("online value size is {}", onlineValueIds.size());

        // 删除无用数据
        logger.info("delete ids, delete count = {}", valueDataRepository.deleteOfflineData(onlineValueIds));
    }
}
