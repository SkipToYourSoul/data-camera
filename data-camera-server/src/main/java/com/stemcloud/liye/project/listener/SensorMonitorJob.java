package com.stemcloud.liye.project.listener;

import com.stemcloud.liye.project.common.DbTools;
import com.stemcloud.liye.project.common.GlobalVariables;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Belongs to data-camera-server
 * Description:
 *  listen sensor monitor status
 * @author liye on 2017/11/18
 */
public class SensorMonitorJob implements Job {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            long beginTime = System.currentTimeMillis();
            doJob();
            long endTime = System.currentTimeMillis();
            logger.info("do sensor monitor job, cost: " + (endTime - beginTime));
        } catch (SQLException e) {
            logger.error("sensor monitor job", e);
        }
    }

    private void doJob() throws SQLException {
        Set<Long> onlineSensors = new HashSet<Long>();
        String sql = "SELECT id, is_monitor FROM dc_base_sensor_info WHERE is_delete = 0";
        ResultSet rs = DbTools.getResultSetFromDb(sql);
        while (rs.next()){
            GlobalVariables.sensorMonitorStatus.put(rs.getLong(1), rs.getInt(2));
            onlineSensors.add(rs.getLong(1));
        }
        rs.close();
        for (Map.Entry<Long, Integer> entry : GlobalVariables.sensorMonitorStatus.entrySet()){
            if (!onlineSensors.contains(entry.getKey())){
                GlobalVariables.sensorMonitorStatus.remove(entry.getKey());
            }
        }
    }
}
