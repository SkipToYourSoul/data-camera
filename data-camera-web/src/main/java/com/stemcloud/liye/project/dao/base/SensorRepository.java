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
     * delete sensor
     * @param id
     * @return recorder count
     */
    @Query(value = "UPDATE SensorInfo s SET s.isDeleted = 1 WHERE s.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteSensor(long id);
}
