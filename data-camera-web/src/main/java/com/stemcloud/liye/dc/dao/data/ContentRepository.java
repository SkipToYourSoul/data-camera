package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.ContentInfo;
import org.springframework.data.repository.CrudRepository;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/17
 */
public interface ContentRepository extends CrudRepository<ContentInfo, Long> {

}
