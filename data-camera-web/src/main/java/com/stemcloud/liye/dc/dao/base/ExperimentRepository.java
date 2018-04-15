package com.stemcloud.liye.dc.dao.base;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import com.stemcloud.liye.dc.domain.base.ExperimentInfo;
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
     * find exps by app
     * @param appInfo app
     * @param isDeleted
     * @return list exp
     */
    List<ExperimentInfo> findByAppAndIsDeletedOrderByCreateTime(AppInfo appInfo, int isDeleted);

    /**
     * monitor/cancle monitor experiment
     * @param id
     * @param isMonitor
     * @return recorder count
     */
    @Query(value = "UPDATE ExperimentInfo e SET e.isMonitor = ?2 WHERE e.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer monitorExp(long id, int isMonitor);

    /**
     * recorder/cancle recorder experiment
     * @param id
     * @param isRecorder
     * @return recorder count
     */
    @Query(value = "UPDATE ExperimentInfo e SET e.isRecorder = ?2 WHERE e.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer recorderExp(long id, int isRecorder);

    /**
     * monitor&recorder
     * @param id
     * @param isMonitor
     * @param isRecorder
     * @return
     */
    @Query(value = "UPDATE ExperimentInfo e SET e.isMonitor=?2, e.isRecorder = ?3 WHERE e.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer monitorAndRecorderExp(long id, int isMonitor, int isRecorder);

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
