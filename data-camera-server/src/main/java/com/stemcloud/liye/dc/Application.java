package com.stemcloud.liye.dc;

import com.stemcloud.liye.dc.socket.Server;

/**
 * Belongs to data-camera-server
 * Description:
 *  application start
 *  1) netty server
 *  2) web socket server
 *  3) simulator server
 * @author liye on 2017/11/20.
 */
public class Application {
    public static void main(String[] args) {
        Server.def().start();
        Server.webSocket().start();
    }
}
