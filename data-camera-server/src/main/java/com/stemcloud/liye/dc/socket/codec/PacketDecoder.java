package com.stemcloud.liye.dc.socket.codec;

import com.stemcloud.liye.dc.socket.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger LOG = LoggerFactory.getLogger(PacketDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        Packet packet = new Packet();
        String code = msg.readCharSequence(32, Charset.forName("utf8")).toString();
        packet.setCode(code);
        packet.setFlag(msg.readByte());
        int bodyLength = msg.readInt();
        packet.setBodyLength(bodyLength);
        packet.setBody(msg.readBytes(bodyLength).array());

        LOG.info("Read Packet '{}'", packet);

        out.add(packet);
    }
}
