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

    public static void main(String[] args) throws IOException {
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

        // server: 47.100.187.24
        Socket socket = new Socket("localhost", 8889);
        OutputStream outputStream = socket.getOutputStream();
        byte[] send = new byte[buf.readableBytes()];
        buf.readBytes(send);
        outputStream.write(send);

        /*byte[] receiver = new byte[1024];
        InputStream in = socket.getInputStream();
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
        out.close();*/
        socket.close();
    }

}
