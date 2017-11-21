package com.stemcloud.liye.dc.domain.base;

import javax.persistence.*;

/**
 * Belongs to smart-sensor
 * Author: liye on 2017/9/20
 * Description: base_sensor_config
 * @author liye
 */
@Entity
@Table(name = "dc_base_sensor_config")
public class SensorConfig {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private int type;

    @Column(name = "dimension", nullable = false)
    private String dimension;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
}
