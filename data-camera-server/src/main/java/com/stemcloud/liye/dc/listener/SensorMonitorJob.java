package com.stemcloud.liye.dc.listener;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.stemcloud.liye.dc.common.DbConnectionPool;
import com.stemcloud.liye.dc.common.GlobalVariables;
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
            int count = doJob();
            long endTime = System.currentTimeMillis();
            logger.info("do sensor monitor job, get {} recorders, cost {} ms", count, (endTime - beginTime));
        } catch (SQLException e) {
            logger.error("sensor monitor job", e);
        }
    }

    private int doJob() throws SQLException {
        Set<String> onlineSensors = new HashSet<String>();
        String sql = "SELECT a.sensor_code, b.is_monitor, a.id, a.track_id, a.sensor_config_id " +
                "FROM dc_base_sensor_info a, dc_base_experiment_info b " +
                "WHERE a.exp_id = b.id AND a.is_deleted = 0";

        DruidPooledConnection conn = DbConnectionPool.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = 0;
        while (rs.next()){
            GlobalVariables.sensorMonitorStatus.put(rs.getString(1), rs.getInt(2));
            GlobalVariables.sensorInfo.put(rs.getString(1), String.format("%d_%d_%d", rs.getLong(3), rs.getLong(4), rs.getLong(5)));
            onlineSensors.add(rs.getString(1));
            count ++;
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

        return count;
    }
}
