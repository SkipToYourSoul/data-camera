package com.stemcloud.liye.project.dao.base;

import com.stemcloud.liye.project.domain.base.ExperimentInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_base_experiment_info
 * @author liye on 2017/11/6
 */
public interface ExperimentRepository extends CrudRepository<ExperimentInfo, Long> {
    /**
     * find by isDeleted order by create time
     * @param isDeleted: 0 or 1
     * @return experiments
     */
    List<ExperimentInfo> findByIsDeletedOrderByCreateTime(int isDeleted);
}
