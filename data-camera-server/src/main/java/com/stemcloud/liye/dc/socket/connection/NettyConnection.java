package com.stemcloud.liye.dc.socket.connection;

import io.netty.channel.Channel;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class NettyConnection implements Connection {

    private String id;
    private Status status;
    private long lastReadTime;
    private long lastWriteTime;
    private Channel channel;

    @Override
    public String id() {
        return id;
    }

    @Override
    public void init(Channel channel) {
        this.channel = channel;
        this.id = channel.id().asLongText();
        this.status = Status.CONNECTED;
        this.lastReadTime = System.currentTimeMillis();
        this.lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public boolean isConnected() {
        return status == Status.CONNECTED;
    }

    @Override
    public void close() {
        this.status = Status.DISCONNECTED;
        this.channel.close();
    }

    @Override
    public void updateReadTime() {
        this.lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void updateWriteTime() {
        this.lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public boolean isTimeout() {
        long readDuration = System.currentTimeMillis() - lastReadTime;
        long writeDuration = System.currentTimeMillis() - lastWriteTime;
        return readDuration > TIME_OUT_DURATION && writeDuration > TIME_OUT_DURATION;
    }

    public Channel getChannel() {
        return channel;
    }
}
