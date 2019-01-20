package com.stemcloud.liye.dc.socket;

import com.alibaba.fastjson.JSON;
import com.stemcloud.liye.dc.socket.common.AckResult;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class Packet {

    public static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024; // 10M

    private int head;
    private String code;
    private int sn;
    private int flag;
    private int crc;
    private int bodyLength;
    private byte[] body;
    private int status;



    public Packet ack(AckResult ackResult){
        Packet p = new Packet();
//        p.setHead(head);
        p.setCode(code);
        p.setSn(sn);
        p.setStatus(ackResult.value);
        return p;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public int getSn() {
        return sn;
    }

    public void setSn(int sn) {
        this.sn = sn;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public boolean isValue(){
        return (flag & 1) == 0;
    }

    public boolean isVideo(){
        return !isValue();
    }

    public boolean isCompressed(){
        return (flag & (1 << 1)) == 2;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
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

    public boolean crcValid(){
        // crc32 校验
        CRC32 crc32 = new CRC32();
        crc32.update(body);
        long value = crc32.getValue();
        return value == crc;
    }

    public String asText(){
        if (isValue()){
            return new String(body, Charset.forName("utf8"));
        }
        return "";
    }

    public Map<String, Object> asJson(){
        String text = asText();
        if (text.isEmpty()){
            return Collections.emptyMap();
        }else {
            return JSON.parseObject(text);
        }
    }

    @Override
    public String toString() {
        return "DPacket{" +
                "head='" + head + "\'" +
                ", code='" + code + '\'' +
                ", sn='" + sn + "\'" +
                ", flag=" + flag +
                ", crc32=" + crc +
                ", bodyLength=" + bodyLength +
                '}';
    }
}
