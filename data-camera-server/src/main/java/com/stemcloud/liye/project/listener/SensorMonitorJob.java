package com.stemcloud.liye.project.listener;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.stemcloud.liye.project.common.DbConnectionPool;
import com.stemcloud.liye.project.common.DbTools;
import com.stemcloud.liye.project.common.GlobalVariables;
import jdk.nashorn.internal.objects.Global;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
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
            logger.info("do sensor monitor job, cost {} ms", (endTime - beginTime));
        } catch (SQLException e) {
            logger.error("sensor monitor job", e);
        }
    }

    private void doJob() throws SQLException {
        Set<String> onlineSensors = new HashSet<String>();
        String sql = "SELECT sensor_code, is_monitor, id, track_id FROM dc_base_sensor_info WHERE is_deleted = 0";

        DruidPooledConnection conn = DbConnectionPool.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()){
            GlobalVariables.sensorMonitorStatus.put(rs.getString(1), rs.getInt(2));
            GlobalVariables.sensorInfo.put(rs.getString(1), String.format("%d_%d", rs.getLong(3), rs.getLong(4)));
            onlineSensors.add(rs.getString(1));
        }
        for (Map.Entry<String, Integer> entry : GlobalVariables.sensorMonitorStatus.entrySet()){
            if (!onlineSensors.contains(entry.getKey())){
                GlobalVariables.sensorMonitorStatus.remove(entry.getKey());
                GlobalVariables.sensorInfo.remove(entry.getKey());
            }
        }

        rs.close();
        ps.close();
        conn.close();
    }
}
