package com.stemcloud.liye.dc.socket.common;

/**
 * Belongs to data-camera-server
 * Description:
 *  cmd
 * @author liye on 2019/4/14
 */
public enum Cmd {

    HEARTBEAT_REQ("heartbeat_req"),
    TIMER_START("timer_start"),
    TIMER_PAUSE("timer_pause"),
    TIMER_RESET("timer_reset"),
    APP_TYPE_REQ("app_type_req");

    public final String value;
    Cmd(String value) {
        this.value = value;
    }
}
