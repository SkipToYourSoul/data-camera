package com.stemcloud.liye.dc.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stemcloud.liye.dc.dao.base.SensorRepository;
import com.stemcloud.liye.dc.dao.data.RecorderRepository;
import com.stemcloud.liye.dc.dao.data.ValueDataRepository;
import com.stemcloud.liye.dc.dao.data.VideoDataRepository;
import com.stemcloud.liye.dc.domain.base.SensorInfo;
import com.stemcloud.liye.dc.domain.data.RecorderDevices;
import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.data.ValueData;
import com.stemcloud.liye.dc.domain.data.VideoData;
import com.stemcloud.liye.dc.domain.view.ChartTimeSeries;
import com.stemcloud.liye.dc.domain.common.SensorType;
import com.stemcloud.liye.dc.domain.view.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  service of sensor data
 * @author liye on 2017/11/16
 */
@Service
public class DataService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SensorRepository sensorRepository;
    private final ValueDataRepository valueDataRepository;
    private final RecorderRepository recorderRepository;
    private final VideoDataRepository videoDataRepository;

    @Autowired
    public DataService(SensorRepository sensorRepository, ValueDataRepository valueDataRepository, RecorderRepository recorderRepository, VideoDataRepository videoDataRepository) {
        this.sensorRepository = sensorRepository;
        this.valueDataRepository = valueDataRepository;
        this.recorderRepository = recorderRepository;
        this.videoDataRepository = videoDataRepository;
    }

    /**
     * 获取实验最新的传感器数据
     *
     * @param expId 给定的实验id
     * @param timestamp 时间戳下限
     * @return sensor_id, (data_key, List<data_value>)
     */
    public Map<Long, Map<String, List<ChartTimeSeries>>> getRecentDataOfBoundSensors(long expId, long timestamp){
        List<SensorInfo> boundSensors = sensorRepository.findByExpIdAndIsDeleted(expId, 0);
        Set<Long> boundSensorIds = new HashSet<Long>();
        for (SensorInfo bs: boundSensors){
            boundSensorIds.add(bs.getId());
        }

        Date time = new Date(timestamp);
        List<ValueData> data = valueDataRepository.findByCreateTimeGreaterThanAndSensorIdInOrderByCreateTime(time, boundSensorIds);

        logger.info("request {} data, time is {}", data.size(), time.toString());
        return transferChartData(data);
    }

    /**
     * 获取实验片段数据
     *
     * @param expId 给定的实验id
     * @return Map<Long, Map>
     *     key: content_id
     *     value: SensorType, Map<SensorId, data>
     */
    public Map<Long, Map> getContentDataOfExperiment(final long expId){
        Map<Long, Map> result = new HashMap<Long, Map>(16);
        List<RecorderInfo> ris = recorderRepository.findByExperiments(new HashSet<Long>(){{
            add(expId);
        }});
        // traverse content
        for (RecorderInfo r : ris){
            long beginMillis = System.currentTimeMillis();
            long id = r.getId();
            List<RecorderDevices> devices = new Gson().fromJson(r.getDevices(), new TypeToken<ArrayList<RecorderDevices>>(){}.getType());
            Date startTime = r.getStartTime();
            Date endTime = r.getEndTime();

            // -- video data
            List<VideoData> videos = videoDataRepository.findByRecorderInfo(r);
            Map<Long, Video> videoMap = transferVideoData(videos);

            // -- value data for chart
            List<ValueData> chartValues = new ArrayList<ValueData>();
            for (RecorderDevices device: devices){
                long sensorId = device.getSensor();
                List<String> legends = device.getLegends();
                chartValues.addAll(valueDataRepository.findBySensorIdAndKeyInAndCreateTimeGreaterThanEqualAndCreateTimeLessThanEqualOrderByCreateTime(
                        sensorId, legends, startTime, endTime
                ));
            }
            Map<Long, Map<String, List<ChartTimeSeries>>> chartMap
                    = transferChartData(chartValues);

            Map<String, Map> map = new HashMap<String, Map>(2);
            map.put(SensorType.CHART.toString(), chartMap);
            map.put(SensorType.VIDEO.toString(), videoMap);

            result.put(id, map);
            long endMillis = System.currentTimeMillis();
            logger.info("Get content {}'s data in {} ms.", id, (endMillis - beginMillis));
        }

        return result;
    }

    /**
     * 新生成一条用户自定义的实验片段
     *
     * @param expId 实验id
     * @param contentId 片段id
     * @param start 数据截取起点
     * @param end 数据截取终点
     * @param legend 留下的数据维度
     */
    public void generateUserContent(long expId, long contentId, int start, int end, List<String> legend){
        RecorderInfo recorder = recorderRepository.findOne(contentId);
        Date startTime = recorder.getStartTime();
        Date endTime = recorder.getEndTime();
        List<RecorderDevices> devices = new Gson().fromJson(recorder.getDevices(), new TypeToken<ArrayList<RecorderDevices>>(){}.getType());

        // 遍历选中的几个数据段，找出最低和最高时间
        Date minTime = endTime, maxTime = startTime;
        Map<Long, List<String>> sensorLegend = new HashMap<Long, List<String>>();
        for (String index : legend) {
            long sensorId = Long.parseLong(index.split("-")[0]);
            String key = index.split("-")[1];
            List<ValueData> values = valueDataRepository.findBySensorIdAndKeyAndCreateTimeGreaterThanEqualAndCreateTimeLessThanEqualOrderByCreateTime(
                    sensorId, key, startTime, endTime);
            int lowerIndex = (int) Math.ceil((double) start/100 * values.size()) - 1;
            int higherIndex = (int) Math.floor((double) end/100 * values.size()) - 1;
            Date lowerTime = values.get(lowerIndex).getCreateTime();
            Date higherTime = values.get(higherIndex).getCreateTime();
            if (lowerTime.compareTo(minTime) < 0){
                minTime = lowerTime;
            }
            if (higherTime.compareTo(maxTime) > 0){
                maxTime = higherTime;
            }
            List<String> ls = new ArrayList<String>();
            if (sensorLegend.containsKey(sensorId)){
                ls = sensorLegend.get(sensorId);
            }
            ls.add(key);
            sensorLegend.put(sensorId, ls);
        }

        // 保存新的实验记录
        RecorderInfo newRecorder = new RecorderInfo();
        newRecorder.setName("用户生成的新记录");
        newRecorder.setDescription("用户新纪录描述");
        newRecorder.setStartTime(minTime);
        newRecorder.setEndTime(maxTime);
        newRecorder.setExpId(expId);
        newRecorder.setIsRecorder(0);
        for (RecorderDevices device: devices){
            if (sensorLegend.containsKey(device.getSensor())){
                device.setLegends(sensorLegend.get(device.getSensor()));
            } else {
                devices.remove(device);
            }
        }
        newRecorder.setDevices(new Gson().toJson(devices));
        recorderRepository.save(newRecorder);
    }

    /**
     * 将valueData转换成为echart需要的时间数据格式
     *
     * sensor_id, (data_key, List<data_value>)
     */
    private Map<Long, Map<String, List<ChartTimeSeries>>> transferChartData(List<ValueData> vd){
        Map<Long, Map<String, List<ChartTimeSeries>>> result = new HashMap<Long, Map<String, List<ChartTimeSeries>>>();

        for (ValueData d : vd){
            long sensorId = d.getSensorId();
            String key = sensorId + "-" + d.getKey();
            Double value = d.getValue();
            Date time = d.getCreateTime();

            if (!result.containsKey(sensorId)){
                Map<String, List<ChartTimeSeries>> map = new HashMap<String, List<ChartTimeSeries>>();
                List<ChartTimeSeries> list = new ArrayList<ChartTimeSeries>();
                list.add(new ChartTimeSeries(time, value));
                map.put(key, list);
                result.put(sensorId, map);
            } else {
                Map<String, List<ChartTimeSeries>> map = result.get(sensorId);
                List<ChartTimeSeries> list = new ArrayList<ChartTimeSeries>();
                if (map.containsKey(key)){
                    list = map.get(key);
                    list.add(new ChartTimeSeries(time, value));
                } else {
                    list.add(new ChartTimeSeries(time, value));
                }
                map.put(key, list);
                result.put(sensorId, map);
            }
        }

        return result;
    }

    /**
     * 将videoData转换成为video.js需要的数据格式
     *
     * sensor_id, video_path
     */
    private Map<Long, Video> transferVideoData(List<VideoData> videos){
        Map<Long, Video> map = new HashMap<Long, Video>();
        for (VideoData v : videos){
            long sensorId = v.getSensorId();
            Video video = new Video();
            video.setOption(v.getVideoPost(), v.getVideoPath());
            video.setRecorderId(v.getRecorderInfo().getId());
            map.put(sensorId, video);
        }
        return map;
    }
}
