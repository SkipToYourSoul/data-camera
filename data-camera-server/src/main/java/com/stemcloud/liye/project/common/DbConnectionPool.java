package com.stemcloud.liye.project.common;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Belongs to data-camera-server
 * Description:
 *  db connection pool with druid
 * @author liye on 2017/11/18
 */
public class DbConnectionPool {
    private static Logger logger = LoggerFactory.getLogger(DbConnectionPool.class);

    private static DbConnectionPool dbConnectionPool = null;
    private static DruidDataSource druidDataSource = null;

    static {
        Properties properties = new Properties();
        try {
            properties.load(DbConnectionPool.class.getResourceAsStream("/db.properties"));
            druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            logger.error("加载数据库配置失败", e);
        }
    }

    public static synchronized DbConnectionPool getInstance(){
        if (null == dbConnectionPool){
            dbConnectionPool = new DbConnectionPool();
        }
        return dbConnectionPool;
    }

    public DruidPooledConnection getConnection() throws SQLException {
        return druidDataSource.getConnection();
    }
}
