package com.stemcloud.liye.project.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Belongs to data-camera-web
 * Description:
 *  controller of view (*.html)
 * @author liye on 2017/11/6
 */
@Controller
public class ViewController implements ErrorController {
    private Logger logger = LoggerFactory.getLogger(ViewController.class);

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/error")
    public String error(){
        return "hello";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
