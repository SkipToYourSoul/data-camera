package com.stemcloud.liye.dc.domain.view;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2017/11/30
 */
public enum SensorType {
    /**
     * chart, video
     */
    CHART(1), VIDEO(2);

    private int value;

    SensorType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
