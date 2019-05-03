package com.stemcloud.liye.dc.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class Client {

    private void writeToFile(InputStream in) throws IOException {
        byte[] receiver = new byte[1024];
        int len ;
        File f = new File("./test.byte");
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        OutputStream out = new FileOutputStream(f);
        while ((len = in.read(receiver)) > 0) {
            System.out.println(len);
            out.write(receiver, 0, len);
        }
        out.close();
    }

    public static ByteBuf msg5() {
        byte msgType = (byte)0x05;
        int sessionId = 12345678;
        byte flag = (byte)0x00;
        String jsonStr = "{\"device_id\":\"201807160001\",\"cmd\":\"push_sensor_data\"," +
                "\"params\":[{\"type\":\"temperature\",\"length\":2,\"data\":[255,159]}," +
                "{\"type\":\"acceleration\",\"length\":2,\"data\":[255,259]}]}";
        byte[] body = jsonStr.getBytes(Charset.forName("utf8"));
        int bodyLength = body.length;

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(msgType);
        buf.writeInt(sessionId);
        buf.writeByte(flag);
        buf.writeInt(bodyLength);
        buf.writeBytes(body);

        return buf;
    }

    public static ByteBuf registerMsg() {
        byte msgType = (byte)0x01;
        int sessionId = 12345678;
        byte flag = (byte)0x00;
        String jsonStr = "{\"device_id\":\"201807160001\",\"cmd\":\"reg_device\"," +
                "\"params\":[{\"security\":\"5d9e8380acef30\",\"firmware_version\":\"1.0.0.20180716\"}]}";
        byte[] body = jsonStr.getBytes(Charset.forName("utf8"));
        int bodyLength = body.length;

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(msgType);
        buf.writeInt(sessionId);
        buf.writeByte(flag);
        buf.writeInt(bodyLength);
        buf.writeBytes(body);

        return buf;
    }

    public static ByteBuf testMsg() {
        byte msgType = (byte)0x10;
        int sessionId = 12345678;
        byte flag = (byte)0x00;
        String jsonStr = "{\"device_id\":\"201807160001\",\"cmd\":\"timer_start\"}";
        byte[] body = jsonStr.getBytes(Charset.forName("utf8"));
        int bodyLength = body.length;

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(msgType);
        buf.writeInt(sessionId);
        buf.writeByte(flag);
        buf.writeInt(bodyLength);
        buf.writeBytes(body);

        return buf;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ByteBuf byteBuf = registerMsg();

        // server: 47.100.187.24
        Socket socket = new Socket("localhost", 8889);

        OutputStream outputStream = socket.getOutputStream();
        byte[] send = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(send);
        outputStream.write(send);

        while (true) {

        }

        // socket.close();
    }
}
