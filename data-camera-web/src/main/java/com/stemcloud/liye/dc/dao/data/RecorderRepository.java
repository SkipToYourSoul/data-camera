package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.data.ValueData;
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
     * @return
     */
    RecorderInfo findByExpIdAndIsRecorder(long expId, int inRecorder);

    /**
     * end recorder, update end time
     * @param id
     * @param endTime
     * @return
     */
    @Query(value = "UPDATE RecorderInfo r SET r.endTime = ?2 WHERE id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer endRecorder(long id, Date endTime);
}
