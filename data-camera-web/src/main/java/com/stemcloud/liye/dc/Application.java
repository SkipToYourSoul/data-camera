package com.stemcloud.liye.dc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Belongs to data-camera-web
 * Description:
 *  Server entrance
 * @author liye on 2017/7/15
 */
@ServletComponentScan
@SpringBootApplication
// @EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
