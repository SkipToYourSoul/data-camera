package com.stemcloud.liye.project.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Belongs to data-camera-server
 * Description:
 *  global variables for socket server
 * @author liye on 2017/11/18
 */
public class GlobalVariables {
    public static ConcurrentMap<String, Integer> sensorMonitorStatus = new ConcurrentHashMap<String, Integer>();
    public static ConcurrentMap<String, String> sensorInfo = new ConcurrentHashMap<String, String>();
}
