package com.stemcloud.liye.dc.socket.service;

import com.alibaba.fastjson.JSON;
import com.stemcloud.liye.dc.common.RedisClient;
import com.stemcloud.liye.dc.socket.common.AckResult;
import com.stemcloud.liye.dc.socket.common.DPacket;
import com.stemcloud.liye.dc.socket.common.MsgType;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  根据消息类型作相应的数据包处理
 * @author liye on 2019/1/20
 */
public class DPacketService implements DService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DPacketService.class);
    private static final RedisClient REDIS = RedisClient.I;

    @Override
    public void handle(ChannelHandlerContext context, DPacket packet) {
        context.channel().write(handle(packet));
    }

    private DPacket handle(DPacket packet) {
        if (packet.getMsgTypeEnum() == MsgType.ONE_WAY) {
            try {
                handleSensorData(packet);
                return packet.ack(AckResult.OK, packet);
            } catch (Exception e) {
                LOGGER.error("handleTxtMonitor error, packet is '{}'", packet, e);
                return packet.ack(AckResult.FAILED, packet);
            }
        }

        return packet.ack(AckResult.OK, packet);
    }

    private void handleSensorData(DPacket packet) {
        Map<String, Object> map = packet.asJson();
        String deviceId = map.get("device_id").toString();
        LOGGER.info("Handle sensor data, --> {}", JSON.toJSON(map));
    }
}
