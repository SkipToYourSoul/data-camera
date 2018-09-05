package com.stemcloud.liye.dc;

import com.stemcloud.liye.dc.socket.Server;

import java.sql.SQLException;
import java.util.Random;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2017/11/20.
 */
public class Application {
    public static void main(String[] args) throws InterruptedException, SQLException {
//        System.out.println("Init resources");
//        GlobalVariables.sensorConfigMap = MysqlRepository.loadSensorConfigMap();
//
//        System.out.println("Init scheduler");
//        QuartzManager.addJob("test", SensorMonitorJob.class, 5);
//
//        System.out.println("Init random generator");
//        new RandomGenerator().allSensor();
        // Server.def().start();

        Server.webSocket().start();
    }
}
