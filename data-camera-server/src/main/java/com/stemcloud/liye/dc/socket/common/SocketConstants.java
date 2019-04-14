package com.stemcloud.liye.dc.socket.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Belongs to data-camera-server
 * Description:
 *  socket 使用的一些常量和全局变量
 * @author liye on 2019/4/14
 */
public class SocketConstants {
    /**
     * key: channel_id
     * value: 硬件注册成功时写入的device_id
     */
    public static ConcurrentMap<String, String> channelToDevice = new ConcurrentHashMap<>();

    /**
     * key: device_id
     * value: channel_id
     */
    public static ConcurrentMap<String, String> deviceToChannel = new ConcurrentHashMap<>();
}
