package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * Belongs to data-camera-server
 * Description:
 *  service impl
 * @author liye on 2019/1/20
 */
public interface Service {
    void handle(ChannelHandlerContext context, Packet packet);
}
