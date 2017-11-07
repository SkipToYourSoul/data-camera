package com.stemcloud.liye.project.dao.base;

import com.stemcloud.liye.project.domain.base.AppInfo;
import org.springframework.data.repository.CrudRepository;

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
    List<AppInfo> findByCreatorAndIsDeletedOrderByCreateTime(String creator, int isDeleted);

    /**
     * find app by id
     * @param id id
     * @return app
     */
    AppInfo findById(long id);
}
