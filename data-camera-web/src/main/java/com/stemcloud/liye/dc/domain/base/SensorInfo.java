package com.stemcloud.liye.dc.domain.base;

import com.stemcloud.liye.dc.domain.config.SensorConfig;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to smart-sensor
 * Author: liye on 2017/7/25
 * Description: base_sensor_info
 * @author liye
 */
@Entity
@Table(name = "dc_base_sensor_info")
public class SensorInfo {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "sensor_name", nullable = false)
    private String name;

    @Column(name = "sensor_code", nullable = false)
    private String code;

    @Column(name = "sensor_creator", nullable = false)
    private String creator;

    @Column(precision = 10, scale = 6)
    private Double longitude;

    @Column(precision = 10, scale = 6)
    private Double latitude;

    @Column(name = "sensor_city")
    private String city;

    @Column(name = "sensor_description")
    private String description;

    @Column(name = "app_id",
            columnDefinition = "BIGINT DEFAULT 0")
    private long appId = 0;

    @Column(name = "exp_id",
            columnDefinition = "BIGINT DEFAULT 0")
    private long expId = 0;

    @Column(name = "track_id",
            columnDefinition = "BIGINT DEFAULT 0")
    private long trackId = 0;

    @OneToOne
    private SensorConfig sensorConfig;

    @Column(name = "is_deleted",
            columnDefinition = "INT DEFAULT 0")
    private int isDeleted = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    @Column(name = "modify_time",
            updatable = false,
            columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date modifyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getExpId() {
        return expId;
    }

    public void setExpId(long expId) {
        this.expId = expId;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public SensorConfig getSensorConfig() {
        return sensorConfig;
    }

    public void setSensorConfig(SensorConfig sensorConfig) {
        this.sensorConfig = sensorConfig;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }
}
