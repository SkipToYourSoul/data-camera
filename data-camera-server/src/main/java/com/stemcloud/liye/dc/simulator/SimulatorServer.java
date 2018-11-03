package com.stemcloud.liye.dc.simulator;

import com.stemcloud.liye.dc.socket.Server;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project : data-camera
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class SimulatorServer implements Server {

    public static final Server I = new SimulatorServer();
    private final SensorSimulator sensorSimulator = new SensorSimulator();

    private AtomicBoolean started = new AtomicBoolean(false);
    private static final ScheduledExecutorService SCHEDULE = Executors.newScheduledThreadPool(4, new ThreadFactory() {
        private final AtomicInteger i = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "simulator-executor-" + i.getAndIncrement());
        }
    });

    @Override
    public void start(ChannelFutureListener listener) {
        if (started.compareAndSet(false, true)){
            DataGenerator.init();

            sensorSimulator.refresh();
            // -- 随机数据生成
            SCHEDULE.scheduleAtFixedRate(
                    sensorSimulator,
                    0L,1L,
                    TimeUnit.SECONDS
            );

            // -- 水火箭数据模拟
            SCHEDULE.scheduleAtFixedRate(
                    sensorSimulator::rocket,
                    0L, 100L,
                    TimeUnit.MILLISECONDS
            );

            // -- 单摆数据模拟
            SCHEDULE.scheduleAtFixedRate(
                    sensorSimulator::swag,
                    0L, 500L,
                    TimeUnit.MILLISECONDS
            );

            // -- 传感器状态刷新
            SCHEDULE.scheduleWithFixedDelay(
                    sensorSimulator::refresh,
                    0,
                    30L,
                    TimeUnit.SECONDS
            );
        }
    }

    @Override
    public void start() {
        start(null);
    }

    @Override
    public void shutdown() {
        SCHEDULE.shutdown();
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }
}
