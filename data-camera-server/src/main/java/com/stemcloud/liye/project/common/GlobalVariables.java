package com.stemcloud.liye.project.common;

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
    public static ConcurrentMap<Long, Integer> sensorMonitorStatus = new ConcurrentHashMap<Long, Integer>();
}
