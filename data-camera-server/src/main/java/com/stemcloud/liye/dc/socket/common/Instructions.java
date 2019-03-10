package com.stemcloud.liye.dc.socket.common;

import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  硬件发送过来的指令，统一格式
 * @author liye on 2019/3/10.
 */
public class Instructions {
    String device_id;
    String cmd;
    List<Map<String, Object>> params;

    public String getDeviceId() {
        return device_id;
    }

    public String getCmd() {
        return cmd;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Instructions{" +
                "device_id='" + device_id + '\'' +
                ", cmd='" + cmd + '\'' +
                ", params=" + params +
                '}';
    }
}
