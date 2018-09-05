package com.stemcloud.liye.dc.websocket;

import com.stemcloud.liye.dc.socket.connection.Connection;
import com.stemcloud.liye.dc.socket.connection.ConnectionManager;
import com.stemcloud.liye.dc.socket.connection.NettyConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Belongs to data-camera-server
 * Description:
 *  handler for web socket
 * @author liye on 2018/9/5
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Build Connection address = {}, id = {}", ctx.channel().remoteAddress().toString(), ctx.channel().id().asLongText());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel());
        ConnectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Close Connection address = {}, id = {}", ctx.channel().remoteAddress().toString(), ctx.channel().id().asLongText());
        ConnectionManager.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        LOGGER.info("Channel read, id = {}", ctx.channel().id().asLongText());
        if (msg instanceof HttpRequest) {

        }

        handleWebSocketFrame(ctx, msg);
    }

    /**
     * 客户端第一次请求是http请求，请求头包括ws的信息
     * @param ctx
     * @param request
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        LOGGER.info("Handle Http Request, id = {}", ctx.channel().id().asLongText());

        if (!request.decoderResult().isSuccess()) {
            LOGGER.info("Handle Http Request failure, id = {}", ctx.channel().id().asLongText());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            // 返回应答给客户端
            if (response.status().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
                response.content().writeBytes(buf);
                buf.release();
            }
            // 如果是非Keep-Alive，关闭连接
            ChannelFuture f = ctx.channel().writeAndFlush(response);
            if (!HttpHeaders.isKeepAlive(request) || response.getStatus().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost" + "/websocket",null,false);
        handshaker = wsFactory.newHandshaker(request);
        if(handshaker == null){
            // 不支持
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else{
            LOGGER.info("Handle Http Request success, id = {}", ctx.channel().id().asLongText());
            handshaker.handshake(ctx.channel(), request);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        LOGGER.info("Handle WebSocket Request, id = {}", ctx.channel().id().asLongText());
        String request = frame.text();
        LOGGER.info("收到消息: " + request);

        // 判断监控和录制状态，若对应的传感器有收到数据，则通过websocket发送
        push(ctx, request);
    }

    /**
     * 消息发送方法
     * @param ctx
     * @param message
     */
    private void push(ChannelHandlerContext ctx, String message) {
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        ctx.channel().writeAndFlush(frame);
    }

    private void push(ChannelGroup ctxGroup, String message) {
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        ctxGroup.writeAndFlush(frame);
    }
}
