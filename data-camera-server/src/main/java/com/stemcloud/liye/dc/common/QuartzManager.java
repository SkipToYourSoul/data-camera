package com.stemcloud.liye.dc.common;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Belongs to data-camera-server
 * Description:
 *  manager of timer tasks
 * @author liye on 2017/11/20.
 */
public class QuartzManager {
    private static Logger logger = LoggerFactory.getLogger(QuartzManager.class);

    private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();
    private static String JOB_GROUP_NAME = "JOB_GROUP_NAME";
    private static String TRIGGER_GROUP_NAME = "TRIGGER_GROUP_NAME";

    public static void addJob(String jobName, Class cls, int seconds){
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            JobDetail jobDetail = newJob(cls)
                    .withIdentity(jobName, JOB_GROUP_NAME)
                    .build();
            Trigger trigger = newTrigger()
                    .withIdentity(jobName, TRIGGER_GROUP_NAME)
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(seconds)
                            .repeatForever())
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);

            if (!scheduler.isShutdown()){
                logger.info("start a Quartz job {} with class {}, interval {}", jobName, cls.getName(), seconds);
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static void removeJob(String jobName){
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.pauseTrigger(new TriggerKey(jobName, TRIGGER_GROUP_NAME));
            scheduler.unscheduleJob(new TriggerKey(jobName, TRIGGER_GROUP_NAME));
            scheduler.deleteJob(new JobKey(jobName, JOB_GROUP_NAME));

            logger.info("remove a Quartz job {}", jobName);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static void modifyJobTime(String jobName, int seconds){
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            Trigger trigger = scheduler.getTrigger(new TriggerKey(jobName, TRIGGER_GROUP_NAME));
            if (trigger == null){
                return;
            }
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobName, JOB_GROUP_NAME));
            Class cls = jobDetail.getJobClass();
            removeJob(jobName);
            addJob(jobName, cls, seconds);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static void startJobs() {
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void shutdownJobs() {
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
