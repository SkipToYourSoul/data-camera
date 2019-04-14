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
    private String device_id;
    private String cmd;
    private  List<Map<String, Object>> params;

    public Instructions() {
    }

    public Instructions(String device_id, String cmd, List<Map<String, Object>> params) {
        this.device_id = device_id;
        this.cmd = cmd;
        this.params = params;
    }

    public String getDeviceId() {
        return device_id;
    }

    public String getCmd() {
        return cmd;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
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
