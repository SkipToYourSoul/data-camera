package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.*;
import com.stemcloud.liye.dc.socket.connection.Connection;
import com.stemcloud.liye.dc.socket.connection.ConnectionManager;
import com.stemcloud.liye.dc.socket.connection.NettyConnection;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Belongs to data-camera-server
 * Description:
 *  消息发送的具体实现
 * @author liye on 2019/4/14
 */
public class MsgSenderFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsgSenderFactory.class);

    public static Packet sendPingPongMsg(String channelId) {
        return new PingPongMsgSender().sendMsg(channelId);
    }

    public static Packet sendTimerStartMsg(List<String> deviceIds) {
        CoreMsgSender sender = new CoreMsgSender();
        sender.setAppType(0);
        sender.setCmd(Cmd.TIMER_START.value);

        if (deviceIds.size() == 1) {
            sender.setIsAll(0);
            sender.setDeviceId(deviceIds.get(0));
        }
        List<String> channelIds = new ArrayList<>();

        LOGGER.info("deviceToChannel = {}", SocketConstants.deviceToChannel);
        LOGGER.info("channelToDevice = {}", SocketConstants.channelToDevice);

        deviceIds.forEach(deviceId -> channelIds.add(SocketConstants.deviceToChannel.get(deviceId)));

        return sender.sendMsg(channelIds);
    }

    public static Packet sendTimerPauseMsg(List<String> deviceIds) {
        CoreMsgSender sender = new CoreMsgSender();
        sender.setAppType(0);
        sender.setCmd(Cmd.TIMER_PAUSE.value);

        if (deviceIds.size() == 1) {
            sender.setIsAll(0);
            sender.setDeviceId(deviceIds.get(0));
        }
        List<String> channelIds = new ArrayList<>();
        deviceIds.forEach(deviceId -> channelIds.add(SocketConstants.deviceToChannel.get(deviceId)));
        return sender.sendMsg(channelIds);
    }

    public static Packet sendTimerResetMsg(List<String> deviceIds) {
        CoreMsgSender sender = new CoreMsgSender();
        sender.setAppType(0);
        sender.setCmd(Cmd.TIMER_RESET.value);

        if (deviceIds.size() == 1) {
            sender.setIsAll(0);
            sender.setDeviceId(deviceIds.get(0));
        }
        List<String> channelIds = new ArrayList<>();
        deviceIds.forEach(deviceId -> channelIds.add(SocketConstants.deviceToChannel.get(deviceId)));
        return sender.sendMsg(channelIds);
    }

    public static Packet sendAppViewMsg(String deviceId) {
        AppMsgSender sender = new AppMsgSender();
        sender.setCmd(Cmd.APP_TYPE_REQ.value);
        sender.setDeviceId(deviceId);
        sender.setCode(1);
        sender.setSwitchCode(0);

        String channelId = SocketConstants.deviceToChannel.get(deviceId);
        return sender.sendMsg(channelId);
    }

    public static Packet sendAppSwitchMsg(String deviceId, int switchCode) {
        AppMsgSender sender = new AppMsgSender();
        sender.setCmd(Cmd.APP_TYPE_REQ.value);
        sender.setDeviceId(deviceId);
        sender.setCode(2);
        sender.setSwitchCode(switchCode);

        String channelId = SocketConstants.deviceToChannel.get(deviceId);
        return sender.sendMsg(channelId);
    }
}

class PingPongMsgSender implements MsgSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingPongMsgSender.class);

    @Override
    public Packet sendMsg(String channelId) {
        String deviceId = SocketConstants.channelToDevice.get(channelId);
        String cmd = Cmd.HEARTBEAT_REQ.value;
        Instructions instructions = new Instructions(deviceId, cmd, new ArrayList<Map<String, Object>>(){{
            add(new HashMap<String, Object>(){{
                put("server_timestamp", System.currentTimeMillis());
            }});
        }});
        Packet packet = new Packet(MsgType.PING.value, 0, (byte)0x00, instructions);
        Optional<Connection> connection = ConnectionManager.get(channelId);
        connection.ifPresent(connection1 -> ((NettyConnection) connection1).getChannel().writeAndFlush(packet));

        return packet;
    }

    @Override
    public Packet sendMsg(List<String> channelIds) {
        return sendMsg(channelIds.get(0));
    }
}

// 包括计时器、定时器、投票器
class CoreMsgSender implements MsgSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreMsgSender.class);

    private String deviceId;
    private String cmd;
    private int isAll = 1;
    private int appType;
    private int lock = 0;
    private int timerLength = 0;

    @Override
    public Packet sendMsg(String channelId) {
        return sendMsg(Collections.singletonList(channelId));
    }

    @Override
    public Packet sendMsg(List<String> channelIds) {
        String deviceId = channelIds.size() == 1?this.deviceId:"0";
        Instructions instructions = new Instructions(deviceId, cmd, new ArrayList<Map<String, Object>>(){{
            add(new HashMap<String, Object>(){{
                put("isAll", isAll);
                put("app_type", appType);
                put("lock", lock);
                put("server_timestamp", System.currentTimeMillis());
                put("timer_length", timerLength);
            }});
        }});
        Packet packet = new Packet(MsgType.NORMAL_REQ.value, 0, (byte)0x00, instructions);

        LOGGER.info("Send packet, channelId = {}, packet = {}", channelIds, packet);

        ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        for (String id : channelIds) {
            if (ConnectionManager.get(id).isPresent()) {
                LOGGER.info("Found active channel, channelId = {}", channelIds);
                group.add(((NettyConnection) ConnectionManager.get(id).get()).getChannel());
            }
        }
        group.writeAndFlush(packet);
        group.close();

        return packet;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setIsAll(int isAll) {
        this.isAll = isAll;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public void setLock(int lock) {
        this.lock = lock;
    }

    public void setTimerLength(int timerLength) {
        this.timerLength = timerLength;
    }
}

// 显示APP，切换APP
class AppMsgSender implements MsgSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMsgSender.class);

    private String deviceId;
    private String cmd;
    private int code;
    private int switchCode;

    @Override
    public Packet sendMsg(String channelId) {
        return sendMsg(Collections.singletonList(channelId));
    }

    @Override
    public Packet sendMsg(List<String> channelIds) {
        Instructions instructions = new Instructions(deviceId, cmd, new ArrayList<Map<String, Object>>(){{
            add(new HashMap<String, Object>(){{
                put("code", code);
                put("switch", switchCode);
                put("server_timestamp", System.currentTimeMillis());
            }});
        }});
        Packet packet = new Packet(MsgType.NORMAL_REQ.value, 0, (byte)0x00, instructions);

        LOGGER.info("Send packet --> {}", packet);

        ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        for (String id : channelIds) {
            if (ConnectionManager.get(id).isPresent()) {
                LOGGER.info("Found active channel, channelId = {}", channelIds);
                group.add(((NettyConnection) ConnectionManager.get(id).get()).getChannel());
            }
        }
        group.writeAndFlush(packet);
        group.close();

        return packet;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setSwitchCode(int switchCode) {
        this.switchCode = switchCode;
    }
}
