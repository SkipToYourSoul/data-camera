package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.DPacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * Belongs to data-camera-server
 * Description:
 *  service impl
 * @author liye on 2019/1/20
 */
public interface DService {
    void handle(ChannelHandlerContext context, DPacket packet);
}
