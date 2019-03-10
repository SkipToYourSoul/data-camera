package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.Packet;

/**
 * Belongs to data-camera-server
 * Description:
 *  消息处理接口，通过不同实现处理不同的类型消息
 * @author liye on 2019/3/10.
 */
public interface MsgHandler {
    /**
     * 消息处理函数
     * @param packet: msg packet
     * @return Ack result
     */
    public Packet handleMsg(Packet packet);
}