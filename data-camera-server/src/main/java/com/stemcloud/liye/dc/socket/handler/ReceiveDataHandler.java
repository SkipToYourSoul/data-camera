package com.stemcloud.liye.dc.socket.handler;

import com.stemcloud.liye.dc.socket.common.AckResult;
import com.stemcloud.liye.dc.socket.common.Packet;
import com.stemcloud.liye.dc.socket.connection.Connection;
import com.stemcloud.liye.dc.socket.connection.ConnectionManager;
import com.stemcloud.liye.dc.socket.connection.NettyConnection;
import com.stemcloud.liye.dc.socket.service.Service;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class ReceiveDataHandler extends SimpleChannelInboundHandler<Packet> {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveDataHandler.class);
    private Executor executor;
    private Service service;

    public ReceiveDataHandler(Service service, Executor executor){
        this.service = service;
        this.executor = executor;
    }

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
        if (msg.crcValid()) {
            executor.execute(() -> service.handle(ctx, msg));
        }else {
            LOG.error("received data crc check error, packet is '{}', channel is '{}'",
                    msg, ctx.channel());
            // ack error
            Packet ack = msg.ack(AckResult.FAILED);
            ctx.channel().write(ack);
        }
    }
}
