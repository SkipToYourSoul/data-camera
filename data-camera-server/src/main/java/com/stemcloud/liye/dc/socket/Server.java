package com.stemcloud.liye.dc.socket;

import com.stemcloud.liye.dc.simulator.SimulatorServer;
import com.stemcloud.liye.dc.websocket.WebSocketServer;
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


    static Server simulator() {
        return SimulatorServer.I;
    }

    static Server webSocket() {
        return WebSocketServer.I;
    }

    static Server nettyServer() {
        return NettyServer.I;
    }
}
