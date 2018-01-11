package com.stemcloud.liye.dc.common;

/**
 * Belongs to data-camera-web
 * Description:
 *  实验状态
 * @author liye on 2018/1/11
 */
public enum ExpStatus {
    /**
     *
     */
    MONITORING_NOT_RECORDING("monitoring_not_recording"),
    MONITORING_AND_RECORDING("monitoring_and_recording"),
    NOT_BOUND_SENSOR("not_bound_sensor"), NOT_MONITOR("not_monitor"),

    //
    ALL_DOING("all_doing"), PART_DOING("part_doing"), ALL_STOP("all_stop"),

    UNKNOWN("unknown");

    String value;

    ExpStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
