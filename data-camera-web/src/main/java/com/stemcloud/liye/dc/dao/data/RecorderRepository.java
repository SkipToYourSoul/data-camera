package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_data_recorder_info
 * @author liye on 2017/11/22
 */
public interface RecorderRepository extends CrudRepository<RecorderInfo, Long> {
    /**
     * find recorder info according expId
     * @param expId
     * @param inRecorder
     * @param isDeleted
     * @return
     */
    RecorderInfo findByExpIdAndIsRecorderAndIsDeleted(long expId, int inRecorder, int isDeleted);

    /**
     * find online recorder
     * @param isDeleted
     * @return
     */
    List<RecorderInfo> findByIsDeleted(int isDeleted);

    /**
     * find recorders of current app
     * @param ids
     * @return
     */
    @Query(value = "SELECT r FROM RecorderInfo r WHERE r.expId IN ?1 AND r.isDeleted = 0 AND r.isRecorder = 0 AND r.endTime IS NOT NULL")
    List<RecorderInfo> findByExperiments(Set<Long> ids);

    /**
     * end recorder, update end time
     * @param id
     * @param endTime
     * @param isDeleted
     * @return
     */
    @Query(value = "UPDATE RecorderInfo r SET r.endTime = ?2, r.isRecorder = 0, r.isDeleted = ?3 WHERE id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer endRecorder(long id, Date endTime, int isDeleted);

    /**
     * update name
     * @param id
     * @param name
     * @return
     */
    @Query(value = "UPDATE RecorderInfo r SET r.name = ?2 WHERE id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer updateName(long id, String name);

    /**
     * update desc
     * @param id
     * @param desc
     * @return
     */
    @Query(value = "UPDATE RecorderInfo r SET r.description = ?2 WHERE id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer updateDescription(long id, String desc);

    /**
     * delete content
     * @param id
     * @return recorder count
     */
    @Query(value = "UPDATE RecorderInfo r SET r.isDeleted = 1 WHERE r.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteRecorder(long id);
}
