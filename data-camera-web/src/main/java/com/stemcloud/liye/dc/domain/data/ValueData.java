package com.stemcloud.liye.dc.domain.data;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_data_value_data
 * @author liye on 2017/11/16
 */
@Entity
@Table(name = "dc_data_value_data")
public class ValueData {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "track_id", nullable = false)
    private long trackId;

    @Column(name = "sensor_id", nullable = false)
    private long sensorId;

    @Column(name = "data_key")
    private String key;

    @Column(name = "data_value", precision = 10, scale = 4)
    private double value;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        return "ValueData{" +
                "id=" + id +
                ", trackId=" + trackId +
                ", sensorId=" + sensorId +
                ", key='" + key + '\'' +
                ", value=" + value +
                ", createTime=" + createTime +
                '}';
    }
}
