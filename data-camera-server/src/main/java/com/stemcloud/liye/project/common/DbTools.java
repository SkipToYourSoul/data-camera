package com.stemcloud.liye.project.common;

import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Belongs to data-camera-server
 * Description:
 *  db tools
 * @author liye on 2017/11/18
 */
public class DbTools {
    private static Logger logger = LoggerFactory.getLogger(DbTools.class);
    private static DbConnectionPool bdp = null;

    static {
        logger.info("Init db tools");
        bdp = DbConnectionPool.getInstance();
    }

    public static int saveValueData(long sensorId, long trackId, String key, Double value){
        String sql = String.format("INSERT INTO %s (data_key, data_value, sensor_id, track_id, create_time) VALUES (?,?,?,?,?)", "dc_value_data");
        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        int result = 0;
        try {
            conn = bdp.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, key);
            ps.setDouble(2, value);
            ps.setLong(3, sensorId);
            ps.setLong(4, trackId);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            result = ps.executeUpdate();

            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
