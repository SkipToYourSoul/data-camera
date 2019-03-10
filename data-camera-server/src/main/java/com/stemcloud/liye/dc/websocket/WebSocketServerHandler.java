package com.stemcloud.liye.dc.websocket;

import com.stemcloud.liye.dc.common.GV;
import com.stemcloud.liye.dc.common.M_JSON;
import com.stemcloud.liye.dc.socket.connection.Connection;
import com.stemcloud.liye.dc.socket.connection.ConnectionManager;
import com.stemcloud.liye.dc.socket.connection.NettyConnection;
import com.stemcloud.liye.dc.websocket.message.ClientMessage;
import com.stemcloud.liye.dc.websocket.message.MessageType;
import com.stemcloud.liye.dc.websocket.message.ServerMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  handler for web socket
 * @author liye on 2018/9/5
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerHandler.class);

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
        handleWebSocketFrame(ctx, msg);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        LOGGER.info("Handle WebSocket Request, id = {}", ctx.channel().id().asLongText());

        try {
            // 处理从客户端发过来的消息
            String jMessage = frame.text();
            ClientMessage message = M_JSON.from(jMessage, ClientMessage.class);
            String type = message.getType();
            Map<String, Object> data = message.getData();
            LOGGER.info("Client message: {}", jMessage);

            if (type.equals(MessageType.REGISTER.getValue())) {
                // 打开页面时的注册信息，将channel加入sensor绑定的channelGroup
                List<Integer> sensorIds = (List<Integer>) data.get("sensors");
                sensorIds.forEach((sensorId) -> {
                    long id = (long) sensorId;
                    if (GV.sensorChannelGroup.containsKey(id)) {
                        ChannelGroup group = GV.sensorChannelGroup.get(id);
                        group.add(ctx.channel());
                    } else {
                        ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                        group.add(ctx.channel());
                        GV.sensorChannelGroup.put(id, group);
                    }
                });
            } else if (type.equals(MessageType.START_M.getValue()) || type.equals(MessageType.START_R.getValue())) {
                List<Integer> sensorIds = (List<Integer>) data.get("sensors");
                boolean isStart = false;
                for (int i = 0; i < sensorIds.size(); i++) {
                    long sensorId = sensorIds.get(i);
                    if (type.equals(MessageType.START_M.getValue())) {
                        GV.sensorIsMonitor.put(sensorId, true);
                    }
                    if (GV.sensorChannelGroup.containsKey(sensorId) && !isStart) {
                        MessageHandler.push(GV.sensorChannelGroup.get(sensorId),
                                new ServerMessage(type, data).toString());
                        isStart = true;
                    }
                }
            } else if (type.equals(MessageType.END_M.getValue()) || type.equals(MessageType.END_R.getValue())) {
                List<Integer> sensorIds = (List<Integer>) data.get("sensors");
                boolean isEnd = false;
                for (int i = 0; i < sensorIds.size(); i++) {
                    long sensorId = sensorIds.get(i);
                    if (type.equals(MessageType.END_M.getValue())) {
                        GV.sensorIsMonitor.put(sensorId, false);
                    }
                    if (GV.sensorChannelGroup.containsKey(sensorId) && !isEnd) {
                        MessageHandler.push(GV.sensorChannelGroup.get(sensorId),
                                new ServerMessage(type, data).toString());
                        isEnd = true;
                    }
                }
            } else {
                LOGGER.info("Unknown message type: {}", jMessage);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
