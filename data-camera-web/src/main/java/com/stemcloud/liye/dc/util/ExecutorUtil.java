package com.stemcloud.liye.dc.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/3/30
 */
public class ExecutorUtil {
    public static final ExecutorService REDIS_EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactory() {
        private AtomicInteger I = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "sync-redis-" + I.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });
}
