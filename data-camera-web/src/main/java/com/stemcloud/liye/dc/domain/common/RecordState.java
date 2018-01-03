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
    ING(-1), END(0), ERR(-10);
    long value;

    RecordState(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
