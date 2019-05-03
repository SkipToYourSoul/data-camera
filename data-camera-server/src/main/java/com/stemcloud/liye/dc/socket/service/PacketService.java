package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.MsgType;
import com.stemcloud.liye.dc.socket.common.Packet;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Belongs to data-camera-server
 * Description:
 *  根据消息类型作相应的数据包处理
 * @author liye on 2019/1/20
 */
public class PacketService implements Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketService.class);

    @Override
    public void handle(ChannelHandlerContext context, Packet packet) {
        LOGGER.info("Packet Service Handle Msg, type = {}", packet.getMsgType());
        MsgHandler handler = MsgHandlerFactory.getInstance(packet.getMsgTypeEnum());
        Packet responseP = handler.handleMsg(context.channel().id().asLongText(), packet);

        if (responseP.getMsgTypeEnum() == MsgType.REG_RES) {
            // 返回注册的响应消息
            context.channel().writeAndFlush(responseP);
        } else {
            LOGGER.info("Response packet is {}", responseP);
        }
    }
}
