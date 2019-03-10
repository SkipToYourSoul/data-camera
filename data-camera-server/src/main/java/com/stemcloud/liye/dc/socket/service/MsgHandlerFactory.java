package com.stemcloud.liye.dc.socket.service;

import com.alibaba.fastjson.JSONObject;
import com.stemcloud.liye.dc.Constants;
import com.stemcloud.liye.dc.common.GV;
import com.stemcloud.liye.dc.common.M_JSON;
import com.stemcloud.liye.dc.common.RedisClient;
import com.stemcloud.liye.dc.socket.common.Packet;
import com.stemcloud.liye.dc.socket.common.Instructions;
import com.stemcloud.liye.dc.socket.common.MsgType;
import com.stemcloud.liye.dc.websocket.MessageHandler;
import com.stemcloud.liye.dc.websocket.message.MessageType;
import com.stemcloud.liye.dc.websocket.message.ServerMessage;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2019/3/10.
 */
public class MsgHandlerFactory {
    public static MsgHandler getInstance(MsgType msgType) {
        switch (msgType) {
            case ONE_WAY:
                return new OneWayMsgHandler();
            default:
                return null;

        }
    }
}

class OneWayMsgHandler implements MsgHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OneWayMsgHandler.class);
    private static final RedisClient REDIS = RedisClient.I;

    @Override
    public Packet handleMsg(Packet packet) {
        Instructions instructions = packet.asInstructions();
        String deviceId = instructions.getDeviceId();
        instructions.getParams().forEach((param) -> {
            String type = param.get("type").toString();
            Integer length = (Integer) param.get("length");
            List<Double> data = (List<Double>) param.get("data");
            LOGGER.info("Msg5, deviceId = {}, type = {}, length = {}, data = {}", deviceId, type, length, M_JSON.toJson(data));
        });

        // 检查当前设备是否处于监控状态
        Map<String, Object> monitorMeta = REDIS.msingle(JSONObject.class, Constants.RedisNamespace.MONITOR, deviceId);
        if (monitorMeta != null && !monitorMeta.isEmpty()) {
            // sensorId: 应用端的传感器编号
            Long sensorId = ((Integer) monitorMeta.get("id")).longValue();
            // monitorMeta.put("data", map);
            monitorMeta.put("timestamp", System.currentTimeMillis());

            // 通过webSocket向已建立链接的页面发送通知
            if (GV.sensorIsMonitor.containsKey(sensorId) && GV.sensorIsMonitor.get(sensorId)) {
                ChannelGroup ctxGroup = GV.sensorChannelGroup.get(sensorId);
                MessageHandler.push(ctxGroup, new ServerMessage(MessageType.DATA.getValue(), monitorMeta).toString());
            }
        }

        // 检查当前设备是否处于录制状态
        Map<String, Object> recordMeta = REDIS.msingle(JSONObject.class, Constants.RedisNamespace.RECORD, deviceId);
        if (recordMeta != null && !recordMeta.isEmpty()) {
            // 解析数据并插入数据库

        }

        LOGGER.info("Handle sensor data, --> {}", instructions.toString());

        return null;
    }
}
