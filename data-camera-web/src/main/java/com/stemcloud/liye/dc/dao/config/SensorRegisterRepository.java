package com.stemcloud.liye.dc.dao.config;

import com.stemcloud.liye.dc.domain.config.SensorRegister;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2017/12/2.
 */
public interface SensorRegisterRepository extends CrudRepository<SensorRegister, String> {
    /**
     * find sensor register
     * @param code
     * @return
     */
    SensorRegister findByCode(String code);

    /**
     * sensor register
     * @param action
     * @param code
     * @return
     */
    @Query(value = "UPDATE SensorRegister s SET s.isRegistered = ?1 WHERE s.code = ?2")
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    Integer register(int action, String code);
}
