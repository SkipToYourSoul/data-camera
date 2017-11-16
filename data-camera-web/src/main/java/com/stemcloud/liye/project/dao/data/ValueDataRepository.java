package com.stemcloud.liye.project.dao.data;

import com.stemcloud.liye.project.domain.data.ValueData;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_value_data
 * @author liye on 2017/11/6
 */
public interface ValueDataRepository extends CrudRepository<ValueData, Long> {
    /**
     * find data by track id
     * @param trackId
     * @param time
     * @return
     */
    List<ValueData> findByTrackIdAndCreateTimeGreaterThanEqualOrderByCreateTime(long trackId, Date time);

    /**
     * find data by track id and sensor id
     * @param trackId
     * @param sensorId
     * @param time
     * @return
     */
    List<ValueData> findByTrackIdAndSensorIdAndCreateTimeGreaterThanEqualOrderByCreateTime(long trackId, long sensorId, Date time);
}
