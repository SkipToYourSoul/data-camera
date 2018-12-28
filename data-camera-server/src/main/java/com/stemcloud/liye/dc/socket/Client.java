package com.stemcloud.liye.dc.socket;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class Client {

    public static void main(String[] args) throws IOException {
        JSONObject jobj = new JSONObject();
        jobj.put("key", 0.001);
        String code = "qazw";
        int sn = 1;
        int head = 0;
        int flag = 0;
        String body = jobj.toJSONString();
        byte[] bytes = body.getBytes(Charset.forName("utf8"));
        int bodyLength = bytes.length;
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        int crc = (int)crc32.getValue();
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(head);
        buf.writeCharSequence(code, Charset.forName("utf8"));
        buf.writeInt(sn);
        buf.writeInt(flag);
        buf.writeInt(crc);
        buf.writeInt(bodyLength);
        buf.writeBytes(bytes);

        Socket socket = new Socket("47.100.187.24", 8889);
        OutputStream outputStream = socket.getOutputStream();
        byte[] send = new byte[buf.readableBytes()];
        buf.readBytes(send);
        outputStream.write(send);
        socket.close();
    }

}
