package com.stemcloud.liye.dc.socket;

import com.stemcloud.liye.dc.simulator.SimulatorServer;
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


    static Server def() {
//        return NettyServer.I;
        return SimulatorServer.I;
    }
}
