package com.stemcloud.liye.dc.socket.handler;

import com.stemcloud.liye.dc.socket.Packet;
import com.stemcloud.liye.dc.socket.connection.Connection;
import com.stemcloud.liye.dc.socket.connection.ConnectionManager;
import com.stemcloud.liye.dc.socket.connection.NettyConnection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class ReceiveDataHandler extends SimpleChannelInboundHandler<Packet> {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveDataHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = new NettyConnection();
        connection.init(ctx.channel());
        ConnectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnectionManager.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
        // 处理业务逻辑
    }
}
