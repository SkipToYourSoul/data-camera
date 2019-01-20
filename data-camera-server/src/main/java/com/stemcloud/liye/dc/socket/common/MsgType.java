package com.stemcloud.liye.dc.socket.common;

/**
 * Belongs to data-camera-server
 * Description:
 *  1.	0x01 REG_REQ 设备注册请求
 *  2.	0x02 REG_RES 设备注册响应
 *  3.	0x03 NORMAL_REQ 普通请求
 *  4.	0x04 NORMAL_RES 普通响应
 *  5.	0x05 ONE_WAY 单向广播，无需响应
 *  6.	0x06 PING 心跳请求PING
 *  7.	0x07 PONG 心跳响应PONG
 * @author liye on 2019/1/20
 */
public enum MsgType {
    REG_REQ, REG_RES, NORMAL_REQ, NORMAL_RES, ONE_WAY, PING, PONG, UN_KNOW;
}
