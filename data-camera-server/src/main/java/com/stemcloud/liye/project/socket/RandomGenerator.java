package com.stemcloud.liye.project.socket;

import com.stemcloud.liye.project.common.DbTools;

import java.util.Random;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2017/11/20.
 */
public class RandomGenerator {
    private Random rand = new Random(System.currentTimeMillis());

    private double randomDouble(int range, int min){
        return rand.nextDouble() * range + min;
    }

    /**
     * generate temperature and humidity value
     */
    public void thSensor() throws InterruptedException {
        long sensorId = 1;
        long trackId = 1;
        long time = System.currentTimeMillis();
        while (true){
            DbTools.saveValueData(sensorId, trackId, "温度", randomDouble(10, 15));
            DbTools.saveValueData(sensorId, trackId, "湿度", randomDouble(20, 20));
            long cost = System.currentTimeMillis() - time;
            if (cost < 5000L){
                Thread.sleep(5000L - cost);
            }
            time = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        new RandomGenerator().randomDouble(10,15);
    }
}
