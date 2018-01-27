package com.stemcloud.liye.dc.util;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/16
 */
public class RedisKeyUtils {
    private static final String SEP = ":";
    private static final String PREFIX = "dc";

    public static String mkSensorMonitorKey(String sensorCode){
        return PREFIX + SEP + "monitor" + SEP + sensorCode;
    }

    public static String mkSensorRecordKey(String sensorCode){
        return PREFIX + SEP + "record" + SEP + sensorCode;
    }
}
