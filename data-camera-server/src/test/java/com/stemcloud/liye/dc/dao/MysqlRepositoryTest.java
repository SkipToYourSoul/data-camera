package com.stemcloud.liye.dc.dao;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2018/1/16
 */
public class MysqlRepositoryTest {
    @Test
    public void saveValueData() throws Exception {
        // -- get data from socket client and redis
        // --- socket client: sensor_code, (key, value)
        // --- redis: sensor_code, (sensor_id, track_id)
        long sensorId = 0L;
        long trackId = 0L;
        String key = "key";
        Double value = 0.0;

        MysqlRepository.saveValueData(sensorId, trackId, key, value, System.currentTimeMillis());
    }

}