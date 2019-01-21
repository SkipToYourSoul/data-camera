package com.stemcloud.liye.dc.socket.codec;

import com.stemcloud.liye.dc.socket.common.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 *
 *     01234567 01234567 01234567 01234567
 *    +--------+--------+--------+--------+
 *    |              Head                 | -> 32bit
 *    +--------+--------+--------+--------+
 *    |              Code                 | -> 32bit
 *    +--------+--------+--------+--------+
 *    |               SN                  | -> 32bit
 *    +--------+--------+--------+--------+
 *    |              Flag                 | -> 32bit
 *    +--------+--------+--------+--------+
 *    |              CRC32                | -> 32bit
 *    +--------+--------+--------+--------+
 *    |           Body Length             | -> 32bit
 *    +--------+--------+--------+--------+
 *    |               Body                |
 *    |               ...                 | -> BodyLength bit
 *    |               ...                 |
 *
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class PacketCodec extends MessageToMessageCodec<ByteBuf, Packet> {

    private static final Logger LOG = LoggerFactory.getLogger(PacketCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
        // write ack
        LOG.info("encode message -> '{}'", msg);

        ByteBuf buf = Unpooled.buffer(32 * 4);
        buf.writeCharSequence(msg.getCode(), Charset.forName("utf8"));
        buf.writeInt(msg.getSn());
        buf.writeInt(msg.getStatus());
        out.add(buf);

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        try {
            int head = msg.readInt();
            String code = msg.readCharSequence(4, Charset.forName("utf8")).toString();
            int sn = msg.readInt();
            int flag = msg.readInt();
            int crc = msg.readInt();
            int bodyLength = msg.readInt();
            byte[] body = new byte[bodyLength];
            msg.readBytes(body);

            Packet packet = new Packet();
            packet.setHead(head);
            packet.setCode(code);
            packet.setSn(sn);
            packet.setFlag(flag);
            packet.setCrc(crc);
            packet.setBodyLength(bodyLength);
            packet.setBody(body);

            LOG.info("Read packet '{}'", packet);

            out.add(packet);
        }catch (Exception e){
            LOG.error("read packet error", e);
        }
    }
}
