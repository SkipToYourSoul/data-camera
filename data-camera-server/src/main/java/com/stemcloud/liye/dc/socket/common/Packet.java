package com.stemcloud.liye.dc.socket.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stemcloud.liye.dc.common.M_JSON;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

/**
 * Belongs to data-camera-server
 * Description:
 *  packet of device
 * @author liye on 2019/1/20
 */
public class Packet {
    /**
     *  10M
     * */
    public static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024;

    private byte msgType;
    private int sessionId;
    private byte flag;
    private int bodyLength;
    private byte[] body;

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public Packet ack(AckResult ackResult, Map<String, Object> result) {
        Packet packet = new Packet();
        packet.setMsgType(this.msgType);
        packet.setSessionId(this.sessionId);
        packet.setFlag(this.flag);

        JSONObject jobj = new JSONObject();
        jobj.put("code", ackResult.value);
        if (result != null && result.size() > 0) {
            jobj.put("result", result);
        }
        String body = jobj.toJSONString();
        byte[] bytes = body.getBytes(Charset.forName("utf8"));
        int bodyLength = bytes.length;

        packet.setBodyLength(bodyLength);
        packet.setBody(bytes);

        System.out.println("Ack return: " + packet.toString());

        return packet;
    }

    public MsgType getMsgTypeEnum() {
        int msgFlag = msgType & 0xFF;
        switch (msgFlag) {
            case 1: return MsgType.REG_REQ;
            case 2: return MsgType.REG_RES;
            case 3: return MsgType.NORMAL_REQ;
            case 4: return MsgType.NORMAL_RES;
            case 5: return MsgType.ONE_WAY;
            case 6: return MsgType.PING;
            case 7: return MsgType.PONG;
            default:
                return MsgType.UN_KNOW;
        }
    }

    public String asText() {
        if ((flag & 0xFF) == 0) {
            return new String(body, Charset.forName("utf8"));
        }
        return "";
    }

    public Map<String, Object> asJson() {
        String text = asText();
        if (text.isEmpty()) {
            return Collections.emptyMap();
        } else {
            return JSON.parseObject(text);
        }
    }

    public Instructions asInstructions() {
        String text = asText();
        return M_JSON.from(text, Instructions.class);
    }

    @Override
    public String toString() {
        return "Packet {" +
                "msgType=" + msgType +
                ", sessionId=" + sessionId +
                ", flag=" + flag +
                ", bodyLength=" + bodyLength +
                ", body=" + JSON.toJSONString(asJson()) +
                '}';
    }
}
