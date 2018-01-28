package com.stemcloud.liye.dc.dao.system;

import com.stemcloud.liye.dc.domain.system.SysUser;
import org.springframework.data.repository.CrudRepository;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/27
 */
public interface SysUserRepository extends CrudRepository<SysUser, String> {
}
