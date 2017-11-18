package com.stemcloud.liye.project.common;

import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static ResultSet getResultSetFromDb(String sql){
        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = bdp.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null){
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return rs;
    }
}
