package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.Packet;

import java.util.List;

/**
 * Belongs to data-camera-server
 * Description:
 *  消息发送接口，通过不同实现处理不同的类型消息
 * @author liye on 2019/4/14
 */
public interface MsgSender {
    /**
     * 消息发送
     * @param channelId: channel id
     * @return Packet
     */
    Packet sendMsg(String channelId);

    Packet sendMsg(List<String> channelIds);
}
