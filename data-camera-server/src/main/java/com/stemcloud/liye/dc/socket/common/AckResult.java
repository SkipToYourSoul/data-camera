package com.stemcloud.liye.dc.socket.common;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public enum AckResult {

    UNKNOWN(-1),
    OK(0),
    FAILED(1);

    public final int value;
    AckResult(int value){
        this.value = value;
    }
}
