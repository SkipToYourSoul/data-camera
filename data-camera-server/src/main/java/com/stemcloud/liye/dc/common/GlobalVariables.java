package com.stemcloud.liye.dc.common;

import com.stemcloud.liye.dc.dao.MysqlRepository;
import com.stemcloud.liye.dc.domain.SensorConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Belongs to data-camera-server
 * Description:
 *  global variables for socket server
 * @author liye on 2017/11/18
 */
public class GlobalVariables {

    /**
     * update with quartz job
     * key: sensor_code
     * value: is_monitor
     */
    public static ConcurrentMap<String, Integer> sensorMonitorStatus = new ConcurrentHashMap<String, Integer>();

    /**
     * update with quartz job
     * key: sensor_code
     * value: (sensor_id)_(track_id)_(sensor_config_id)
     */
    public static ConcurrentMap<String, String> sensorInfo = new ConcurrentHashMap<String, String>();

    /** sensor config, load when server start **/
    public static Map<Long, SensorConfig> sensorConfigMap = MysqlRepository.loadSensorConfigMap();
}
