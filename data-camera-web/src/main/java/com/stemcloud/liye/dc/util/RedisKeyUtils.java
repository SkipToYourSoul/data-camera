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

    public static String mkSensorMonitorKey(){
        return PREFIX + SEP + "monitor";
    }

    public static String mkSensorRecordKey(){
        return PREFIX + SEP + "record";
    }
}
