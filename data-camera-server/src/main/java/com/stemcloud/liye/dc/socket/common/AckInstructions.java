package com.stemcloud.liye.dc.socket.common;

import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  Ack instruction
 * @author liye on 2019/4/14
 */
public class AckInstructions {
    private String device_id;
    private String cmd;
    private int code;
    private Map<String, Object> params;

    public AckInstructions(String device_id, String cmd, int code, Map<String, Object> params) {
        this.device_id = device_id;
        this.cmd = cmd;
        this.code = code;
        this.params = params;
    }

    public AckInstructions() {
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
