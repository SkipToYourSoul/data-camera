package com.stemcloud.liye.dc.socket.handler;

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
 * Belongs to data-camera-server
 * Description:
 *  channel管理
 * @author liye on 2019/1/20
 */
public class PacketHandler extends SimpleChannelInboundHandler<Packet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketHandler.class);
    private Executor executor;
    private Service service;

    public PacketHandler(Service service, Executor executor){
        this.service = service;
        this.executor = executor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = new NettyConnection();
        connection.init(ctx.channel());
        LOGGER.info("Add channel, channelId = {}", connection.id());
        ConnectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Remove channel, channelId = {}", ctx.channel().id());
        ConnectionManager.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet packet) throws Exception {
        // 处理业务逻辑
        executor.execute(() -> service.handle(channelHandlerContext, packet));
    }
}
