package com.stemcloud.liye.project;

import com.stemcloud.liye.project.common.QuartzManager;
import com.stemcloud.liye.project.listener.TestJob;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2017/11/20.
 */
public class Application {
    public static void main(String[] args) {
        System.out.println("Begin");
        QuartzManager.addJob("test", TestJob.class, 5);
        System.out.println("End");
    }
}
