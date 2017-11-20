package com.stemcloud.liye.project.socket;

import com.stemcloud.liye.project.common.DbTools;
import com.stemcloud.liye.project.common.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
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
     * generate temperature and humidity value
     */
    public void thSensor(String sensorCode) throws InterruptedException {
        long time = System.currentTimeMillis();
        while (true){
            if (GlobalVariables.sensorMonitorStatus.containsKey(sensorCode) && GlobalVariables.sensorMonitorStatus.get(sensorCode) == 1) {
                long sensorId = Long.parseLong(GlobalVariables.sensorInfo.get(sensorCode).split("_")[0]);
                long trackId = Long.parseLong(GlobalVariables.sensorInfo.get(sensorCode).split("_")[1]);

                DbTools.saveValueData(sensorId, trackId, "温度", randomDouble(10, 15));
                DbTools.saveValueData(sensorId, trackId, "湿度", randomDouble(20, 20));
            }
            long cost = System.currentTimeMillis() - time;
            if (cost < 5000L){
                Thread.sleep(5000L - cost);
                logger.info("generate data, cost {} millis", cost);
            }
            time = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        new RandomGenerator().randomDouble(10,15);
    }
}
