package com.stemcloud.liye.project.dao.base;

import com.stemcloud.liye.project.domain.base.TrackInfo;
import org.springframework.data.repository.CrudRepository;

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
}
