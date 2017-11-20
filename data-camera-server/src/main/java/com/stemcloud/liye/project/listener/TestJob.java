package com.stemcloud.liye.project.listener;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Belongs to data-camera-server
 * Description:
 *  test
 * @author liye on 2017/11/20
 */
public class TestJob implements Job {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd: HH:mm:ss");

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String now = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        System.out.println("Do job at " + now);
    }
}
