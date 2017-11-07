package com.stemcloud.liye.project.dao.base;

import com.stemcloud.liye.project.domain.base.ExperimentInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * find exp by id
     * @param id id
     * @return exp
     */
    ExperimentInfo findById(long id);

    /**
     * update experiment
     * @param id
     * @param name
     * @param description
     * @return recorder count
     */
    @Query(value = "UPDATE ExperimentInfo e SET e.name = ?2, e.description = ?3 WHERE e.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer updateExp(long id, String name, String description);

    /**
     * delete experiment
     * @param id
     * @return recorder count
     */
    @Query(value = "UPDATE ExperimentInfo e SET e.isDeleted = 1 WHERE e.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteExp(long id);
}
