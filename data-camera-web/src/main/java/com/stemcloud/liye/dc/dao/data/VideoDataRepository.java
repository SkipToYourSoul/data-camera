package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.RecorderInfo;
import com.stemcloud.liye.dc.domain.data.VideoData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2017/11/30
 */
public interface VideoDataRepository extends CrudRepository<VideoData, Long> {

    /**
     * find videos
     * @param recorderInfo
     * @return
     */
    public List<VideoData> findByRecorderInfo(RecorderInfo recorderInfo);
}
