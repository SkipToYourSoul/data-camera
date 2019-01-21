package com.stemcloud.liye.dc.socket.connection;

import io.netty.channel.Channel;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public interface Connection {

    long TIME_OUT_DURATION = 30 * 60 * 1000; // 30m

    String id();
    void init(Channel channel);
    boolean isConnected();
    void close();
    void updateReadTime();
    void updateWriteTime();
    boolean isTimeout();


    enum Status {
        NEW, CONNECTED, DISCONNECTED
    }
}
