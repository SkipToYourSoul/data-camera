package com.stemcloud.liye.dc.socket.common;

import java.util.List;

/**
 * Belongs to data-camera-server
 * Description:
 *  MsgType: 0x05
 * @author liye on 2019/2/2
 */
public class BodyMsg5 {
    private String deviceId;
    private String cmd;
    private List<Params> params;

    public class Params {
        private String type;
        private int length;
        private List<Double> data;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public List<Double> getData() {
            return data;
        }

        public void setData(List<Double> data) {
            this.data = data;
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<Params> getParams() {
        return params;
    }

    public void setParams(List<Params> params) {
        this.params = params;
    }
}
