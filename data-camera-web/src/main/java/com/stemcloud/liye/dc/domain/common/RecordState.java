package com.stemcloud.liye.dc.domain.common;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2017/12/13
 */
public enum RecordState {
    /**
     * record state
     */
    ING(1), END(0), ERR(-1);
    int value;

    RecordState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
