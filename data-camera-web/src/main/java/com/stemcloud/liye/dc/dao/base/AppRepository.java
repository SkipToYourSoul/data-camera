package com.stemcloud.liye.dc.dao.base;

import com.stemcloud.liye.dc.domain.base.AppInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_base_app_info
 * @author liye on 2017/11/6
 */
public interface AppRepository extends CrudRepository<AppInfo, Long> {
    /**
     * find by creator and isDeleted order by create time
     * @param creator: creator
     * @param isDeleted: 0 or 1
     * @return apps
     */
    List<AppInfo> findByCreatorAndIsDeletedOrderByCreateTimeDesc(String creator, int isDeleted);

    /**
     * delete app
     * @param id
     * @return recorder count
     */
    @Query(value = "UPDATE AppInfo a SET a.isDeleted = 1 WHERE a.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteApp(long id);
}
