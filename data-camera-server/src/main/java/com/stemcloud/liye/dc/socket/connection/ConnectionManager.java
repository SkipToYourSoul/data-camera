package com.stemcloud.liye.dc.socket.connection;

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
                    LOG.warn("Connection Timeout. Id is '{}'", k);
                    timeOutKeys.add(k);
                }
            });
            if (!timeOutKeys.isEmpty()) {
                timeOutKeys.forEach(CONNECTIONS::remove);
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
        return CONNECTIONS.remove(id);
    }

    public static Connection remove(Channel channel) {
        String id = channel.id().asLongText();
        return remove(id);
    }

}
