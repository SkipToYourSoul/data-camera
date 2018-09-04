package com.stemcloud.liye.dc.simulator;

import com.stemcloud.liye.dc.common.JSON;
import com.stemcloud.liye.dc.dao.MysqlRepository;
import com.stemcloud.liye.dc.domain.SensorConfig;
import com.stemcloud.liye.dc.domain.SensorStatus;
import com.stemcloud.liye.dc.socket.Packet;
import com.stemcloud.liye.dc.socket.service.HandleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.stemcloud.liye.dc.common.GlobalVariables.*;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class SensorSimulator implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorSimulator.class);

    private static final HandleDataService HANDLER = new HandleDataService();
    private static final ConcurrentMap<String, SensorStatus> SENSORS = new ConcurrentHashMap<>();

    @Override
    public void run() {
        // 生成数据
        SENSORS.forEach((code, sensor) -> {
            Packet p = new Packet();
            p.setCode(code);
            p.setFlag(0);
            SensorConfig config = sensorConfigMap.get(sensor.getSensorConfigId());
            if (config != null){
                // 获取dim
                List<String> dims = config.getDimension();
                Map<String, Double> vals = new HashMap<>();

                // 【水火箭】实验专用数据, HARD CODE
                if (sensor.getId() == 8) {
                    vals.putAll(DataGenerator.getData(dims));
                } else {
                    dims.forEach(dim -> {
                        if ("视频".equals(dim)) {
                            return;
                        }
                        double val;
                        if("温度".equals(dim)){
                            val = rand(10, 25);
                        }else if ("湿度".equals(dim)){
                            val = rand(30, 60);
                        }else if("光照".equals(dim)){
                            val = rand(30, 500);
                        }else if ("脑电波".equals(dim)){
                            val = rand(100, 1600);
                        }else if ("压力".equals(dim)){
                            val = rand(1, 30);
                        }else {
                            val = rand(1, 100);
                        }
                        vals.put(dim, val);
                    });
                }

                if (!vals.isEmpty()){
                    p.setBody(JSON.toJson(vals).getBytes(Charset.forName("utf8")));
                    HANDLER.handle(p);
                }
            }
        });
    }

    private double rand(int from, int to){
        if (to <= from){
            return 0.0;
        }
        Random random = new Random();
        return random.nextDouble() * from + (to - from);
    }

    public void refresh(){
        Set<String> onlineSensors = new HashSet<>();
        List<SensorStatus> sensorStatuses = MysqlRepository.fetchSensorStatus();
        sensorStatuses.forEach(sensor -> {
            SENSORS.put(sensor.getCode(), sensor);
            onlineSensors.add(sensor.getCode());
        });

        Set<String> notExist = new HashSet<>();
        SENSORS.forEach((k, v) -> {
            if (!onlineSensors.contains(k)){
                notExist.add(k);
            }
        });
        notExist.forEach(SENSORS::remove);
    }
}
