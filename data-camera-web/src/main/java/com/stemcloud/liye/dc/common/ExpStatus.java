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
    NO_AVAILABLE_SENSOR("no_available_sensor"),
    ALL_MONITORING_AND_ALL_RECORDING("all_monitoring_and_all_recording"),
    ALL_MONITORING_AND_PART_RECORDING("all_monitoring_and_part_recording"),
    ALL_MONITORING_AND_NO_RECORDING("all_monitoring_and_no_recording"),
    PART_MONITORING("part_monitoring"), ALL_NOT_MONITOR("all_not_monitor"),

    UNKNOWN("unknown");

    String value;

    ExpStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
