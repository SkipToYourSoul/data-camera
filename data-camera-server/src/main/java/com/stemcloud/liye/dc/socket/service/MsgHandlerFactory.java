package com.stemcloud.liye.dc.socket.service;

import com.stemcloud.liye.dc.socket.common.DPacket;
import com.stemcloud.liye.dc.socket.common.MsgType;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2019/3/10.
 */
public class MsgHandlerFactory {
    public MsgHandler getInstance(MsgType msgType) {
        return new OneWayMsgHandler();
    }
}

class OneWayMsgHandler implements MsgHandler {

    @Override
    public DPacket handleMsg() {


        return null;
    }
}
