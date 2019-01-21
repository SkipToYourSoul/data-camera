package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public interface Service {

    void handle(ChannelHandlerContext context, Packet packet);

}
