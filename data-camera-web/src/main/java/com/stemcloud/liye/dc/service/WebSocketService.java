package com.stemcloud.liye.dc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/2/11
 */
@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static int onlineCount = 0;
    private static CopyOnWriteArraySet<WebSocketService> webSocketSet = new CopyOnWriteArraySet<WebSocketService>();
    private Session session;

    /**
     * 链接成功时调用
     * @param session
     */
    @OnOpen
    public void onOpen(Session session/*, @PathParam("expId") String expId*/){
        this.session = session;
        webSocketSet.add(this);
        addOnlineCount();
        logger.info("有新链接加入，当前连接数为：" + getOnlineCount());
        try {
            sendMessage("链接成功，实验ID为："/* + expId*/);
        } catch (IOException e) {
            logger.error("IO异常");
        }
    }

    /**
     * 链接关闭时调用
     */
    @OnClose
    public void onClose(){
        webSocketSet.remove(this);
        subOnlineCount();
        logger.info("有链接关闭，当前连接数为：" + getOnlineCount());
    }

    /**
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        logger.info("来自客户端的消息:" + message);
        sendMessage("来自服务器的消息");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误");
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketService.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketService.onlineCount--;
    }
}
