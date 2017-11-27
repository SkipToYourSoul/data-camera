package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.ValueData;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_data_value_data
 * @author liye on 2017/11/6
 */
public interface ValueDataRepository extends CrudRepository<ValueData, Long> {
    /**
     * find data by sensor ids in time range
     * @param sensorId
     * @param startTime
     * @param endTime
     * @return
     */
    List<ValueData> findBySensorIdInAndCreateTimeGreaterThanEqualAndCreateTimeLessThanEqualOrderByCreateTime(Set<Long> sensorId, Date startTime, Date endTime);

    /**
     * find data in sensor ids
     * @param timestamp
     * @param ids
     * @return
     */
    List<ValueData> findByCreateTimeGreaterThanAndSensorIdInOrderByCreateTime(Date timestamp, Set<Long> ids);
}
