package com.stemcloud.liye.dc.socket.handler;

import com.stemcloud.liye.dc.socket.common.DPacket;
import com.stemcloud.liye.dc.socket.connection.Connection;
import com.stemcloud.liye.dc.socket.connection.ConnectionManager;
import com.stemcloud.liye.dc.socket.connection.NettyConnection;
import com.stemcloud.liye.dc.socket.service.DService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Belongs to data-camera-server
 * Description:
 *  管理数据包channel
 * @author liye on 2019/1/20
 */
public class DPacketHandler extends SimpleChannelInboundHandler<DPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DPacketHandler.class);
    private Executor executor;
    private DService service;

    public DPacketHandler(DService service, Executor executor){
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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DPacket packet) throws Exception {
        // 处理业务逻辑
        executor.execute(() -> service.handle(channelHandlerContext, packet));
    }
}
