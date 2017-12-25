package com.stemcloud.liye.dc.socket;

import io.netty.channel.ChannelFutureListener;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public interface Server {
    void start(ChannelFutureListener listener);
    void start();
    void shutdown();
    boolean isStarted();


    static Server _default() {
        return NettyServer.I;
    }
}
