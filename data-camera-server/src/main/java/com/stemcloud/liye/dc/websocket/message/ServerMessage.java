package com.stemcloud.liye.dc.websocket.message;

import com.stemcloud.liye.dc.common.JSON;

import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  web socket server message
 * @author liye on 2018/9/7
 */
public class ServerMessage {
    private String type;
    private Map<String, Object> data;

    public ServerMessage(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public String toString() {
        return JSON.toJson(this);
    }
}
