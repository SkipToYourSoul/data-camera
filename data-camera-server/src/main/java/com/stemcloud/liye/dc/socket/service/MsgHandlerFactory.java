package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.common.M_JSON;
import com.stemcloud.liye.dc.common.RedisClient;
import com.stemcloud.liye.dc.socket.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  消息处理的具体实现
 * @author liye on 2019/3/10.
 */
public class MsgHandlerFactory {
    public static MsgHandler getInstance(MsgType msgType) {
        switch (msgType) {
            case ONE_WAY:
                return new OneWayMsgHandler();
            case REG_REQ:
                return new RegisterMsgHandler();
            default:
                return new DefaultMsgHandler();

        }
    }
}

class OneWayMsgHandler implements MsgHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OneWayMsgHandler.class);
    private static final RedisClient REDIS = RedisClient.I;

    @Override
    public Packet handleMsg(String channelId, Packet packet) {
        Instructions instructions = packet.asInstructions();
        LOGGER.info("Handle ONE_WAY Msg, instruction = {}", M_JSON.toJson(instructions));

        String deviceId = instructions.getDeviceId();
        Map<String, List<Double>> sensorData = new HashMap<>();
        instructions.getParams().forEach((param) -> {
            String type = param.get("type").toString();
            Integer length = (Integer) param.get("length");
            sensorData.put(type, (List<Double>) param.get("data"));
            LOGGER.debug("[ONE_WAY] Param, type = {}, length = {}", type, length);
        });

        // 检查当前设备是否处于监控状态
        /*Map<String, Object> monitorMeta = REDIS.msingle(JSONObject.class, Constants.RedisNamespace.MONITOR, deviceId);
        if (monitorMeta != null && !monitorMeta.isEmpty()) {
            // sensorId: 应用端的传感器编号
            Long sensorId = ((Integer) monitorMeta.get("id")).longValue();
            monitorMeta.put("data", sensorData);
            monitorMeta.put("timestamp", System.currentTimeMillis());

            // 通过webSocket向已建立链接的页面发送通知
            if (Constants.sensorIsMonitor.containsKey(sensorId) && Constants.sensorIsMonitor.get(sensorId)) {
                ChannelGroup ctxGroup = Constants.sensorChannelGroup.get(sensorId);
                MessageHandler.push(ctxGroup, new ServerMessage(MessageType.DATA.getValue(), monitorMeta).toString());
            }
        }

        // 检查当前设备是否处于录制状态
        Map<String, Object> recordMeta = REDIS.msingle(JSONObject.class, Constants.RedisNamespace.RECORD, deviceId);
        if (recordMeta != null && !recordMeta.isEmpty()) {
            // 解析数据并插入数据库

        }*/

        LOGGER.info("Handle sensor data, --> {}", instructions.toString());

        AckInstructions ack = new AckInstructions();
        ack.setCode(AckResult.OK.value);
        ack.setCmd("push_sensor_data");
        ack.setDevice_id(deviceId);

        return packet.ack(MsgType.ONE_WAY, ack);
    }
}

class RegisterMsgHandler implements MsgHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMsgHandler.class);

    @Override
    public Packet handleMsg(String channelId, Packet packet) {
        String deviceId = "";
        String cmd = "";
        try {
            Instructions instructions = packet.asInstructions();
            deviceId = instructions.getDeviceId();
            cmd = instructions.getCmd();
            List<Map<String, Object>> params = instructions.getParams();
            LOGGER.info("Handle REG_REQ Msg, instruction = {}", M_JSON.toJson(instructions));

            AckInstructions ack = new AckInstructions();
            ack.setDevice_id(deviceId);
            ack.setCmd(cmd);
            if (params.size() > 0) {
                String security = (String) params.get(0).get("security");
                // security验证
                System.out.println(security);

                // 获取最新硬件版本号
                String latestFimwareVersion = "";

                // 注册成功时，需要记录deviceId和channelId的对应关系
                SocketConstants.channelToDevice.put(channelId, deviceId);
                SocketConstants.deviceToChannel.put(deviceId, channelId);

                Map<String, Object> result = new HashMap<String, Object>() {{
                    put("latest_firmware_version", latestFimwareVersion);
                    put("server_timestamp", System.currentTimeMillis());
                }};

                ack.setCode(AckResult.OK.value);
                ack.setParams(result);
                return packet.ack(MsgType.REG_RES, ack);
            } else {
                ack.setCode(AckResult.FAILED.value);
                ack.setParams(new HashMap<String, Object>() {{
                    put("error", "empty params");
                }});
                return packet.ack(MsgType.REG_RES, ack);
            }
        } catch (Exception e) {
            LOGGER.error("RegisterMsg, DeviceId={}", deviceId, e);
            AckInstructions ack = new AckInstructions(deviceId, cmd, AckResult.FAILED.value, new HashMap<String, Object>() {{
                put("error", e.getMessage());
            }});
            return packet.ack(MsgType.REG_RES, ack);
        }
    }
}

class DefaultMsgHandler implements MsgHandler {

    @Override
    public Packet handleMsg(String channelId, Packet packet) {
        return packet;
    }
}
