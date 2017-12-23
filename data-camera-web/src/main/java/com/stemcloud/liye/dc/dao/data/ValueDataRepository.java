package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.ValueData;
import org.springframework.cache.annotation.Cacheable;
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
     * 找到当前设备下维度(1或者多个)下，在给定时间戳范围内的数据
     *
     * @param sensorId 设备编号
     * @param key 数据维度
     * @param startTime 时间戳下限
     * @param endTime 时间戳上线
     * @return
     */
    List<ValueData> findBySensorIdAndKeyInAndCreateTimeGreaterThanEqualAndCreateTimeLessThanEqualOrderByCreateTime(Long sensorId, List<String> key, Date startTime, Date endTime);

    /**
     * 找到当前设备下，比给定时间戳更新的数据
     *
     * @param timestamp 时间戳下限
     * @param ids 设备编号
     * @return
     */
    List<ValueData> findByCreateTimeGreaterThanAndSensorIdInOrderByCreateTime(Date timestamp, Set<Long> ids);

    /**
     * 找到当前设备下，在给定时间戳范围内的数据
     *
     * @param sensorId 设备编号
     * @param startTime 时间戳下限
     * @param endTime 时间戳上线
     * @return
     */
    List<ValueData> findBySensorIdAndCreateTimeGreaterThanEqualAndCreateTimeLessThanEqualOrderByCreateTime(Long sensorId, Date startTime, Date endTime);

    /**
     * 找到当前设备和维度下，在给定时间戳范围内的数据
     *
     * @param sensorId 设备编号
     * @param key 数据维度
     * @param startTime 时间戳下限
     * @param endTime 时间戳上线
     * @return
     */
    List<ValueData> findBySensorIdAndKeyAndCreateTimeGreaterThanEqualAndCreateTimeLessThanEqualOrderByCreateTime(Long sensorId, String key, Date startTime, Date endTime);
}
