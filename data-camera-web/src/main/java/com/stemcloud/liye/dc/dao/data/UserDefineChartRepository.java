package com.stemcloud.liye.dc.dao.data;

import com.stemcloud.liye.dc.domain.data.UserDefineChart;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/10/3
 */
public interface UserDefineChartRepository extends CrudRepository<UserDefineChart, Long> {

    List<UserDefineChart> findByRecorderId(long recorderId);
}
