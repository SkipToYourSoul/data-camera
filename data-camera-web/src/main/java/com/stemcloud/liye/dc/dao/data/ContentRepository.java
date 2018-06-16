package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.ContentInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

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
     * 查询用户热门内容
     * @param owner
     * @param isDeleted
     * @return
     */
    List<ContentInfo> findByOwnerAndIsDeletedAndIsSharedOrderByLikeDesc(String owner, int isDeleted, int isShared);

    /**
     * 查询分享的热门内容，后续需改造成分页查询
     * @return
     */
    @Transactional(timeout = 10)
    @Query(value = "SELECT c FROM ContentInfo c WHERE c.isShared = 1 AND c.isDeleted = 0 ORDER BY c.like DESC, c.createTime DESC")
    List<ContentInfo> findTop50ByIsSharedAndIsDeletedOrderByLikeAndCreateTimeDesc();

    /**
     * 查询关键词相关内容
     * @param isShared
     * @param isDeleted
     * @param search
     * @return
     */
    List<ContentInfo> findByIsSharedAndIsDeletedAndTitleLikeOrderByLikeDesc(int isShared, int isDeleted, String search);

    /**
     * delete content
     * @param id
     * @return 1
     */
    @Query(value = "UPDATE ContentInfo c SET c.isDeleted = 1 WHERE c.id = ?1")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer deleteContent(long id);
}
