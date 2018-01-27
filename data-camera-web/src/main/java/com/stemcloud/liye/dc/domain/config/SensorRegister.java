package com.stemcloud.liye.dc.domain.config;

import javax.persistence.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_backend_sensor_register
 * @author liye on 2017/12/2.
 */
@Entity
@Table(name = "dc_backend_sensor_register")
public class SensorRegister {
    @Id
    @Column(name = "sensor_code", nullable = false)
    private String code;

    @OneToOne
    private SensorConfig sensorConfig;

    @Column(name = "is_registered",
            columnDefinition = "INT DEFAULT 0")
    private int isRegistered = 0;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SensorConfig getSensorConfig() {
        return sensorConfig;
    }

    public void setSensorConfig(SensorConfig sensorConfig) {
        this.sensorConfig = sensorConfig;
    }

    public int getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(int isRegistered) {
        this.isRegistered = isRegistered;
    }
}
