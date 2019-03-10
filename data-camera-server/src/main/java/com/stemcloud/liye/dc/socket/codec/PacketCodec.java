package com.stemcloud.liye.dc.socket.codec;

import com.stemcloud.liye.dc.socket.common.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Belongs to data-camera-server
 * Description:
 *
 *                  01234567
 *    +--------+--------+--------+--------+
 *    |              msgType              | -> 8bit
 *    +--------+--------+--------+--------+
 *    |              SessionId            | -> 32bit
 *    +--------+--------+--------+--------+
 *    |              Flag                 | -> 8bit
 *    +--------+--------+--------+--------+
 *    |           Body Length             | -> 32bit
 *    +--------+--------+--------+--------+
 *    |               Body                |
 *    |               ...                 | -> BodyLength bit
 *    |               ...                 |
 *
 * @author liye on 2019/1/20
 */
public class PacketCodec extends MessageToMessageCodec<ByteBuf, Packet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketCodec.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, List<Object> list) throws Exception {
        LOGGER.info("encode message --> {}", packet);
        ByteBuf buf = Unpooled.buffer(32 * 4);
        buf.writeByte(packet.getMsgType());
        buf.writeInt(packet.getSessionId());
        buf.writeByte(packet.getFlag());
        buf.writeInt(packet.getBodyLength());
        buf.writeBytes(packet.getBody());
        list.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        try {
            byte msgType = byteBuf.readByte();
            int sessionId = byteBuf.readInt();
            byte flag = byteBuf.readByte();
            int bodyLength = byteBuf.readInt();
            byte[] body = new byte[bodyLength];
            byteBuf.readBytes(body);

            Packet packet = new Packet();
            packet.setMsgType(msgType);
            packet.setSessionId(sessionId);
            packet.setFlag(flag);
            packet.setBodyLength(bodyLength);
            packet.setBody(body);

            list.add(packet);
            LOGGER.info("Read packet --> {}", packet);
        } catch (Exception e) {
            LOGGER.error("read packet error --> {}", byteBuf.toString(), e);
        }
    }
}
