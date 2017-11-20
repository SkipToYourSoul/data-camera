package com.stemcloud.liye.project;

import com.stemcloud.liye.project.common.QuartzManager;
import com.stemcloud.liye.project.listener.SensorMonitorJob;
import com.stemcloud.liye.project.listener.TestJob;
import com.stemcloud.liye.project.socket.RandomGenerator;

import java.sql.SQLException;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2017/11/20.
 */
public class Application {
    public static void main(String[] args) throws InterruptedException, SQLException {
        System.out.println("Init scheduler");
        QuartzManager.addJob("test", SensorMonitorJob.class, 5);

        System.out.println("Init random generator");
        new RandomGenerator().thSensor("0001");
    }
}
