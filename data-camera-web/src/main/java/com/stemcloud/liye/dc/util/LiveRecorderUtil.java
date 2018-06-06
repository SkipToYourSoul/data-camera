package com.stemcloud.liye.dc.util;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  控制直播流的录制
 * @author liye on 2018/6/6
 */
public class LiveRecorderUtil {
    public static Map<String, String> recorderStatusMap = new HashMap<String, String>();
    public static final int VIDEO_WIDTH = 1280;
    public static final int VIDEO_HEIGHT = 720;

    public static String mkLiveVideoKey(long recorderId, long sensorId) {
        return String.format("[%s]-[%s]", recorderId, sensorId);
    }

    public static void showStatusMap() {
        System.out.println(new Gson().toJson(recorderStatusMap));
    }
}
