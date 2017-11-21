package com.stemcloud.liye.dc.dao.base;

import com.stemcloud.liye.dc.domain.base.TrackInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_base_track_info
 * @author liye on 2017/11/6
 */
public interface TrackRepository extends CrudRepository<TrackInfo, Long> {
    /**
     * find by isDeleted order by create time
     * @param isDeleted: 0 or 1
     * @return tracks
     */
    List<TrackInfo> findByIsDeletedOrderByCreateTime(int isDeleted);

    /**
     * find track by id
     * @param id id
     * @return track
     */
    TrackInfo findById(long id);

    /**
     * bound sensor on track
     * @param sensorId
     * @param trackId
     * @return recorder count
     */
    @Query(value = "UPDATE TrackInfo t SET t.sensor = ?1 WHERE t.id = ?2")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer boundSensor(long sensorId, long trackId);

    /**
     * unbound sensor on track
     * @param trackId
     * @return recorder count
     */
    @Query(value = "UPDATE TrackInfo t SET t.sensor = null WHERE t.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer unboundSensor(long trackId);

    /**
     * delete track
     * @param id
     * @return recorder count
     */
    @Query(value = "UPDATE TrackInfo t SET t.isDeleted = 1 WHERE t.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteTrack(long id);
}
