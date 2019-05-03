package com.stemcloud.liye.dc.socket;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2019/5/3
 */
public class Client2 {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8889);
        OutputStream outputStream = socket.getOutputStream();
        ByteBuf testBuf = Client.testMsg();
        byte[] testSend = new byte[testBuf.readableBytes()];
        testBuf.readBytes(testSend);
        outputStream.write(testSend);

        socket.close();
    }
}
