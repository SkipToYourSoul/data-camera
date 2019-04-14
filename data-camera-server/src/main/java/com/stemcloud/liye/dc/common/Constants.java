package com.stemcloud.liye.dc.common;

import com.stemcloud.liye.dc.dao.MysqlRepository;
import com.stemcloud.liye.dc.domain.SensorConfig;
import com.stemcloud.liye.dc.websocket.message.ClientMessage;
import io.netty.channel.group.ChannelGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Belongs to data-camera-server
 * Description:
 *  global variables for socket server
 * @author liye on 2017/11/18
 */
public class Constants {
    public static final String MONITOR = "monitor";
    public static final String RECORD = "record";
    public static final int START = 1;
    public static final int END = 0;

    // 传感器对应的ChannelGroup，用于WebSocket消息通信
    public static ConcurrentMap<Long, ChannelGroup> sensorChannelGroup = new ConcurrentHashMap<>();

    // 传感器对应的监控状态，用于判断webSocket是否向前端发消息
    public static ConcurrentMap<Long, Boolean> sensorIsMonitor = new ConcurrentHashMap<>();

    /** sensor config, load when server start **/
    public static Map<Long, SensorConfig> sensorConfigMap = MysqlRepository.loadSensorConfigMap();
}
