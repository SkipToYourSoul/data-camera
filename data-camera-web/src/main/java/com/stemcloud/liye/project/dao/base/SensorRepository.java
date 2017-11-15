package com.stemcloud.liye.project.dao.base;

import com.stemcloud.liye.project.domain.base.SensorInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_base_sensor_info
 * @author liye on 2017/11/6
 */
public interface SensorRepository extends CrudRepository<SensorInfo, Long> {
    /**
     * find by creator and isDeleted order by create time
     * @param creator: creator
     * @param isDeleted: 0 or 1
     * @return sensors
     */
    List<SensorInfo> findByCreatorAndIsDeletedOrderByCreateTime(String creator, int isDeleted);

    /**
     * find sensor by id
     * @param id id
     * @return sensor
     */
    SensorInfo findById(long id);

    /**
     * find sensor by app id
     * @param appId
     * @return sensors
     */
    List<SensorInfo> findByAppId(long appId);

    /**
     * use to find available app of current user
     * @param creator
     * @param appId
     * @param expId
     * @param trackId
     * @return sensors
     */
    List<SensorInfo> findByCreatorAndAppIdAndExpIdAndTrackId(String creator, long appId, long expId, long trackId);

    /**
     * bound sensor
     * @param sensorId
     * @param appId
     * @param expId
     * @param trackId
     * @return
     */
    @Query(value = "UPDATE SensorInfo s SET s.appId = ?2, s.expId = ?3, s.trackId = ?4  WHERE s.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer boundSensor(long sensorId, long appId, long expId, long trackId);

    /**
     * unbound sensor by id
     * @param id
     * @return
     */
    @Query(value = "UPDATE SensorInfo s SET s.appId = 0, s.expId = 0, s.trackId = 0  WHERE s.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer unboundSensorById(long id);

    /**
     * delete sensor
     * @param id
     * @return recorder count
     */
    @Query(value = "UPDATE SensorInfo s SET s.isDeleted = 1 WHERE s.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteSensor(long id);
}
