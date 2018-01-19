package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.ContentInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/17
 */
public interface ContentRepository extends CrudRepository<ContentInfo, Long> {
    /**
     * 查询当前用户的内容
     * @param owner
     * @param isDeleted
     * @return
     */
    List<ContentInfo> findByOwnerAndIsDeleted(String owner, int isDeleted);

    /**
     * 查询分享的热门内容
     * @param isShared
     * @param isDeleted
     * @return
     */
    List<ContentInfo> findTop50ByIsSharedAndIsDeletedOrderByLikeAndViewDesc(int isShared, int isDeleted);
}
