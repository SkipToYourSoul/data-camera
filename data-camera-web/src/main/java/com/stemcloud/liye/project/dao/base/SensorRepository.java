package com.stemcloud.liye.project.dao.base;

import com.stemcloud.liye.project.domain.base.SensorInfo;
import org.springframework.data.repository.CrudRepository;

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
}
