package com.stemcloud.liye.project.common;

import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2017/11/18
 */
public class CrudTest {
    public static void main(String[] args) throws SQLException {
        DbConnectionPool bdp = DbConnectionPool.getInstance();
        DruidPooledConnection conn = null;

        conn = bdp.getConnection();
        String sql = "SELECT * FROM dc_base_sensor_info";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()){
            System.out.println(rs.getLong(1));
            System.out.println(rs.getString(2));
        }
        rs.close();
        ps.close();
        conn.close();
    }
}
