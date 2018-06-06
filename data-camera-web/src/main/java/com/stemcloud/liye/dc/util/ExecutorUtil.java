package com.stemcloud.liye.dc.util;

import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Belongs to data-camera-web
 * Description:
 *  线程池管理类
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

    private static final int RECORDER_THREAD_COUNT = 5;
    public static final ExecutorService RECORDER_EXECUTOR = Executors.newFixedThreadPool(RECORDER_THREAD_COUNT, new ThreadFactory() {
        private AtomicInteger I = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "sync-record-" + I.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    private static final int UPLOAD_THREAD_COUNT = 5;
    public static final ExecutorService UPLOAD_EXECUTOR = Executors.newFixedThreadPool(UPLOAD_THREAD_COUNT, new ThreadFactory() {
        private AtomicInteger I = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "sync-upload-" + I.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });
}
