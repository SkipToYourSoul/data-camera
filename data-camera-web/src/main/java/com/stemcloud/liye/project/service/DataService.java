package com.stemcloud.liye.project.service;

import com.stemcloud.liye.project.dao.base.SensorRepository;
import com.stemcloud.liye.project.dao.data.ValueDataRepository;
import com.stemcloud.liye.project.domain.base.SensorInfo;
import com.stemcloud.liye.project.domain.data.ValueData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  service of sensor data
 * @author liye on 2017/11/16
 */
@Service
public class DataService {
    private final SensorRepository sensorRepository;
    private final ValueDataRepository valueDataRepository;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public DataService(SensorRepository sensorRepository, ValueDataRepository valueDataRepository) {
        this.sensorRepository = sensorRepository;
        this.valueDataRepository = valueDataRepository;
    }

    public void getRecentDataOfBoundSensors(long expId, long timestamp){
        List<SensorInfo> boundSensors = sensorRepository.findByExpIdAndIsMonitor(expId, 1);


        for (SensorInfo sensor: boundSensors){
            long trackId = sensor.getTrackId();
            long sensorId = sensor.getId();
            System.out.println("track: " + trackId);
            System.out.println("sensor: " + sensorId);
            System.out.println(dateFormat.format(new Date(timestamp)));

            List<ValueData> data = valueDataRepository.findByTrackIdAndSensorIdAndCreateTimeGreaterThanEqualOrderByCreateTime(trackId, sensorId, new Date(timestamp));
            for (ValueData d: data){
                System.out.println(d.toString());
            }
        }
    }
}
