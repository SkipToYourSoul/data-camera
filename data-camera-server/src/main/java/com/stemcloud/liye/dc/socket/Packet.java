package com.stemcloud.liye.dc.socket;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class Packet {

    public static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024; // 10M

    private String code;
    private byte flag;
    private int bodyLength;
    private byte[] body;


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

    @Override
    public String toString() {
        return "Packet{" +
                "code='" + code + '\'' +
                ", flag=" + flag +
                ", bodyLength=" + bodyLength +
                '}';
    }
}
