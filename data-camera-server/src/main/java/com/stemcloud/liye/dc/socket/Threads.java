package com.stemcloud.liye.dc.socket;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public interface Threads {

    ExecutorService RECEIVER_DATA_HANDLER = Executors.newFixedThreadPool(4, new ThreadFactory() {
        final AtomicInteger N = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "receiver-data-handler-" + N.getAndIncrement());
        }
    });


    static void shutdown(){
        if (!RECEIVER_DATA_HANDLER.isShutdown()){
            RECEIVER_DATA_HANDLER.shutdown();
        }
    }

}
