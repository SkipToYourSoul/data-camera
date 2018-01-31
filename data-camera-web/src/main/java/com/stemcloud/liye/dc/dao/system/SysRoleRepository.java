package com.stemcloud.liye.dc.dao.system;

import com.stemcloud.liye.dc.domain.system.SysRole;
import org.springframework.data.repository.CrudRepository;

/**
 * Belongs to data-camera
 * Description:
 * @author liye
 */
public interface SysRoleRepository extends CrudRepository<SysRole, Long> {
    SysRole findByName(String name);
}
