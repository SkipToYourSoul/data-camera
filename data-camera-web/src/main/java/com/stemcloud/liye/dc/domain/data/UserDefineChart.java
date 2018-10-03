package com.stemcloud.liye.dc.domain.data;

import javax.persistence.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_user_define_chart
 * @author liye on 2018/10/3
 */
@Entity
@Table(name = "dc_user_define_chart")
public class UserDefineChart {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "recorder_id")
    private long recorderId;

    @Column(name = "sensor_id")
    private long sensorId;

    private String x;

    private String y;

    private String name;

    @Column(name = "description")
    private String desc;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecorderId() {
        return recorderId;
    }

    public void setRecorderId(long recorderId) {
        this.recorderId = recorderId;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
