package com.stemcloud.liye.dc.simulator;

import com.stemcloud.liye.dc.dao.MysqlRepository;
import com.stemcloud.liye.dc.domain.SensorConfig;
import com.stemcloud.liye.dc.domain.SensorStatus;
import com.stemcloud.liye.dc.socket.service.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.stemcloud.liye.dc.common.GV.sensorConfigMap;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class SensorSimulator implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorSimulator.class);

    private static final PacketService HANDLER = new PacketService();
    private static final ConcurrentMap<String, SensorStatus> SENSORS = new ConcurrentHashMap<>();

    @Override
    public void run() {
        // 生成数据
        SENSORS.forEach((code, sensor) -> {
            if (sensor.getId() == 8) {
                // 【水火箭】实验专用数据
            } else if (sensor.getId() == 11) {
                // 【单摆】实验专用数据
            } else {
                SensorConfig config = sensorConfigMap.get(sensor.getSensorConfigId());
                if (config != null){
                    Map<String, Double> values = DataGenerator.generateRandom(config);
                    if (!values.isEmpty()){
                        handleValue(code, values);
                    }
                }
            }
        });
    }

    // 模拟生成水火箭数据
    public void rocket() {
        String rocketCode = "1001";
        if (SENSORS.containsKey(rocketCode)) {
            SensorConfig config = sensorConfigMap.get(SENSORS.get(rocketCode).getSensorConfigId());
            Map<String, Double> values = DataGenerator.getRocket(config.getDimension());
            if (!values.isEmpty()) {
                handleValue(rocketCode, values);
            }
        }
    }

    // 模拟生成单摆数据
    public void swag() {
        String swagCode = "2001";
        if (SENSORS.containsKey(swagCode)) {
            SensorConfig config = sensorConfigMap.get(SENSORS.get(swagCode).getSensorConfigId());
            Map<String, Double> values = DataGenerator.getSwag(config.getDimension());
            if (!values.isEmpty()) {
                handleValue(swagCode, values);
            }
        }
    }

    // -- 酚酞数据模拟
    public void ph() {
        String phCode = "3001";
        if (SENSORS.containsKey(phCode)) {
            SensorConfig config = sensorConfigMap.get(SENSORS.get(phCode).getSensorConfigId());
            Map<String, Double> values = DataGenerator.getPh(config.getDimension());
            if (!values.isEmpty()) {
                handleValue(phCode, values);
            }
        }
    }

    /**
     * 周期方法，更新目前已绑定的SENSOR的状态
     */
    public void refresh(){
        Set<String> onlineSensors = new HashSet<>();
        List<SensorStatus> sensorStatuses = MysqlRepository.fetchSensorStatus();
        sensorStatuses.forEach(sensor -> {
            if (!SENSORS.containsKey(sensor.getCode())) {
                SENSORS.put(sensor.getCode(), sensor);
            }
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

    private void handleValue(String code, Map<String, Double> values) {
        /*Packet p = new Packet();
        p.setCode(code);
        p.setFlag(0);
        p.setBody(M_JSON.toJson(values).getBytes(Charset.forName("utf8")));
        LOGGER.info("Handle code = {}, value = {}", code, M_JSON.toJson(values));

        HANDLER.handle(p);*/
    }
}
