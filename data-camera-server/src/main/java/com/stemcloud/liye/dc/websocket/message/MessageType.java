package com.stemcloud.liye.dc.websocket.message;

/**
 * Belongs to data-camera-server
 * Description:
 *  server message type
 * @author liye on 2018/9/7
 */
public enum MessageType {
    START("START"), END("END"), DATA("DATA"), REGISTER("REGISTER");

    private String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
