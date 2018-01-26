package com.stemcloud.liye.dc.socket;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public enum AckResult {

    UNKNOWN(0),
    OK(1),
    FAILED(2);

    public final int value;
    AckResult(int value){
        this.value = value;
    }
}
