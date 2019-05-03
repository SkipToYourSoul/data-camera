package com.stemcloud.liye.dc.socket.connection;

import com.stemcloud.liye.dc.socket.common.SocketConstants;
import com.stemcloud.liye.dc.socket.service.MsgSenderFactory;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public final class ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Scan-Timeout-Thread");
        }
    });
    private static final ConcurrentMap<String, Connection> CONNECTIONS =
            new ConcurrentHashMap<>();

    static {
        SCHEDULER.scheduleAtFixedRate(() -> {
            List<String> timeOutKeys = new ArrayList<>();
            CONNECTIONS.forEach((k, v) -> {
                if (v.isTimeout()){
                    // 链接超时，从连接池中移除链接
                    LOG.warn("Connection Timeout. Id is '{}'", k);
                    timeOutKeys.add(k);
                } else {
                    // 发送心跳包
                    if (SocketConstants.channelToDevice.containsKey(k)) {
                        MsgSenderFactory.sendPingPongMsg(k);
                    }
                }
            });
            if (!timeOutKeys.isEmpty()) {
                timeOutKeys.forEach(CONNECTIONS::remove);

                // 移除channelId和deviceId的对应关系
                timeOutKeys.forEach((k) -> {
                    LOG.info("Timeout, ConnectManage remove channel {}", k);
                    if (SocketConstants.channelToDevice.containsKey(k)) {
                        String deviceId = SocketConstants.channelToDevice.get(k);
                        SocketConstants.deviceToChannel.remove(deviceId);
                        SocketConstants.channelToDevice.remove(k);
                    }
                });
            }
        }, Connection.TIME_OUT_DURATION, Connection.TIME_OUT_DURATION, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!SCHEDULER.isShutdown()){
                SCHEDULER.shutdown();
            }
        }));
    }

    public static void add(Connection connection){
        CONNECTIONS.putIfAbsent(connection.id(), connection);
    }

    public static Optional<Connection> get(String id){
        if (CONNECTIONS.containsKey(id)){
            return Optional.of(CONNECTIONS.get(id));
        }else {
            return Optional.empty();
        }
    }

    public static Connection remove(String id){
        LOG.info("ConnectManage remove channel {}", id);
        if (SocketConstants.channelToDevice.containsKey(id)) {
            String deviceId = SocketConstants.channelToDevice.get(id);
            SocketConstants.deviceToChannel.remove(deviceId);
            SocketConstants.channelToDevice.remove(id);
        }
        return CONNECTIONS.remove(id);
    }

    public static Connection remove(Channel channel) {
        String id = channel.id().asLongText();
        return remove(id);
    }

}
