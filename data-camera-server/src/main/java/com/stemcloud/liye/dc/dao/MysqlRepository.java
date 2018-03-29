package com.stemcloud.liye.dc.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.stemcloud.liye.dc.common.GlobalVariables;
import com.stemcloud.liye.dc.domain.SensorConfig;
import com.stemcloud.liye.dc.domain.SensorStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Belongs to data-camera-server
 * Description:
 *  db tools
 * @author liye on 2017/11/18
 */
public class MysqlRepository {
    private static Logger logger = LoggerFactory.getLogger(MysqlRepository.class);
    private static DruidDataSource dataSource = DbConnectionPool.getInstance();

    private static String VALUE_DATA_TBL = "dc_data_value_data";
    private static String SENSOR_CONFIG_TBL = "dc_base_sensor_config";
    private static String SENSOR_INFO_TBL = "dc_base_sensor_info";
    private static String EXP_INFO_TBL = "dc_base_experiment_info";
    private static DecimalFormat DF = new DecimalFormat("#.00");

    public static Integer[] saveValueDatas(Map<String, Object> datas){

        Integer sensorId = (Integer) datas.get("id");
        Integer trackId = (Integer) datas.get("trackId");
        Map<String, Object> data = (Map<String, Object>)datas.get("data");
        if (sensorId != null && trackId != null){
            // save
            List<Integer> results = new ArrayList<>(data.size());
            data.forEach((k, v) -> {
                double d;
                if (v instanceof Double){
                    d = (Double) v;
                }else if (v instanceof BigDecimal){
                    d = ((BigDecimal)v).doubleValue();
                }else if (v instanceof Integer){
                    d = (Integer)v;
                }else if (v instanceof Float){
                    d = (Float)v;
                }else {
                    return;
                }
                results.add(saveValueData(sensorId, trackId, k, Double.parseDouble(DF.format(d))));
            });
            return results.toArray(new Integer[results.size()]);
        }
        return new Integer[0];
    }

    public static int saveValueData(long sensorId, long trackId, String key, Double value){
        String sql = String.format("INSERT INTO %s (data_key, data_value, sensor_id, track_id, create_time) VALUES (?,?,?,?,?)", VALUE_DATA_TBL);
        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        int result = 0;
        try {
            conn = dataSource.getConnection();
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
            logger.error("[saveValueData]", e);
        }

        return result;
    }

    /**
     * 获取传感器配置信息，静态信息，不会更改
     * @return Map:
     *  key: sensorId
     *  value: sensorConfig(id, dimension, type)
     */
    public static Map<Long, SensorConfig> loadSensorConfigMap(){
        Map<Long, SensorConfig> sensorConfigMap = new HashMap<Long, SensorConfig>(16);
        String sql = String.format("SELECT id, dimension, `type` FROM %s", SENSOR_CONFIG_TBL);
        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                sensorConfigMap.put(rs.getLong(1), new SensorConfig(rs.getLong(1), rs.getString(2), rs.getInt(3)));
            }

            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sensorConfigMap;
    }

    public static List<SensorStatus> fetchSensorStatus(){
        String sql = String.format("SELECT a.sensor_code, b.is_monitor, a.id, a.track_id, a.sensor_config_id " +
                "FROM %s a, %s b WHERE a.exp_id = b.id AND a.is_deleted = 0", SENSOR_INFO_TBL, EXP_INFO_TBL);
        List<SensorStatus> sensorStatuses = new ArrayList<SensorStatus>();

        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()){
                sensorStatuses.add(new SensorStatus(rs.getString(1), rs.getInt(2),
                        rs.getLong(3), rs.getLong(4), rs.getLong(5)));
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sensorStatuses;
    }
}
