package com.stemcloud.liye.dc.dao;

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
    private static DruidDataSource druidDataSource = null;

    private static DruidDataSource loadProperties(){
        Properties properties = new Properties();
        try {
            properties.load(DbConnectionPool.class.getResourceAsStream("/db.properties"));
            druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            logger.error("加载数据库配置失败", e);
        }
        return druidDataSource;
    }

    public static synchronized DruidDataSource getInstance(){
        if (null == druidDataSource){
            druidDataSource = loadProperties();
        }
        return druidDataSource;
    }
}
