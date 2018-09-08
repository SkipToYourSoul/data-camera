package com.stemcloud.liye.dc.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Belongs to data-camera-server
 * Description:
 *  channel message的处理操作
 * @author liye on 2018/9/7
 */
public class MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    /**
     * 消息发送方法
     * @param ctx
     * @param message
     */
    public static void push(ChannelHandlerContext ctx, String message) {
        LOGGER.info("push {} to channel", message);
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        ctx.channel().writeAndFlush(frame);
    }

    public static void push(ChannelGroup ctxGroup, String message) {
        // LOGGER.info("push {} to {} channels", message, ctxGroup.size());
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        ctxGroup.writeAndFlush(frame);
    }
}
