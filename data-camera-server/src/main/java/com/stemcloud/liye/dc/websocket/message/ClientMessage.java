package com.stemcloud.liye.dc.websocket.message;

import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  web socket client message
 * @author liye on 2018/9/6
 */
public class ClientMessage {
    private String type;
    private Map<String, Object> data;

    public ClientMessage(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
