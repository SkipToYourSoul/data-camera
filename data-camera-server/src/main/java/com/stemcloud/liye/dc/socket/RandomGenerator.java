package com.stemcloud.liye.dc.socket;

import com.stemcloud.liye.dc.common.DbTools;
import com.stemcloud.liye.dc.common.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2017/11/20.
 */
public class RandomGenerator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Random rand = new Random(System.currentTimeMillis());
    private DecimalFormat df = new DecimalFormat("#.00");

    private double randomDouble(int range, int min){
        double r = rand.nextDouble() * range + min;
        return Double.parseDouble(df.format(r));
    }

    /**
     * generate random data of all monitor sensors
     * @throws InterruptedException
     */
    public void allSensor() throws InterruptedException {
        long time = System.currentTimeMillis();
        while (true){
            int monitor = 0;
            for (Map.Entry<String, Integer> entry : GlobalVariables.sensorMonitorStatus.entrySet()){
                if (entry.getValue() == 1){
                    String[] value = GlobalVariables.sensorInfo.get(entry.getKey()).split("_");
                    long sensorId = Long.parseLong(value[0]);
                    long trackId = Long.parseLong(value[1]);
                    long configId = Long.parseLong(value[2]);
                    if (trackId == 0){
                        continue;
                    }
                    List<String> dimensions = GlobalVariables.sensorConfigMap.get(configId).getDimension();
                    for (String dimension : dimensions){
                        DbTools.saveValueData(sensorId, trackId, dimension, randomDouble(10, 15));
                    }
                    monitor ++;
                }
            }

            long cost = System.currentTimeMillis() - time;
            if (cost < 2000){
                Thread.sleep(2000 - cost);
                logger.info("generate data of {} sensors, cost {} millis", monitor, cost);
            }
            time = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        new RandomGenerator().randomDouble(10,15);
    }
}
