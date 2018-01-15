package com.stemcloud.liye.dc.listener;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.stemcloud.liye.dc.dao.DbConnectionPool;
import com.stemcloud.liye.dc.common.GlobalVariables;
import com.stemcloud.liye.dc.dao.MysqlRepository;
import com.stemcloud.liye.dc.domain.SensorStatus;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Belongs to data-camera-server
 * Description:
 *  listen sensor monitor status, 轮询监听实验状态
 *  更新 GlobalVariables.sensorMonitorStatus, 当前sensor的监控状态
 *  更新 GlobalVariables.sensorInfo, 当前sensor的绑定信息
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
        List<SensorStatus> sensorStatuses = MysqlRepository.fetchSensorStatus();
        int count = 0;
        for (SensorStatus status : sensorStatuses){
            GlobalVariables.sensorMonitorStatus.put(status.getCode(), status.getIsMonitor());
            GlobalVariables.sensorInfo.put(status.getCode(), String.format("%d_%d_%d", status.getId(), status.getTrackId(), status.getSensorConfigId()));
            onlineSensors.add(status.getCode());
            count ++;
        }
        for (Map.Entry<String, Integer> entry : GlobalVariables.sensorMonitorStatus.entrySet()){
            if (!onlineSensors.contains(entry.getKey())){
                GlobalVariables.sensorMonitorStatus.remove(entry.getKey());
                GlobalVariables.sensorInfo.remove(entry.getKey());
            }
        }

        return count;
    }
}
