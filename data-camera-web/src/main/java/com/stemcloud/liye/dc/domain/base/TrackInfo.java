package com.stemcloud.liye.dc.domain.base;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.sound.midi.Track;
import java.util.Date;

/**
 * Belongs to smart-sensor
 * Author: liye on 2017/10/31
 * Description:
 * @author liye
 */
@Entity
@Table(name = "dc_base_track_info")
public class TrackInfo implements Comparable<TrackInfo> {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private ExperimentInfo experiment;

    @OneToOne
    private SensorInfo sensor;

    @Column(name = "type",
            columnDefinition = "INT DEFAULT 0")
    private int type;

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

    public ExperimentInfo getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentInfo experiment) {
        this.experiment = experiment;
    }

    public SensorInfo getSensor() {
        return sensor;
    }

    public void setSensor(SensorInfo sensor) {
        this.sensor = sensor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o){ return true;}
        if (o == null || getClass() != o.getClass()){ return false;}

        TrackInfo trackInfo = (TrackInfo) o;

        return id == trackInfo.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(TrackInfo t) {
        // type=2 优先
        if (this.getType() > t.getType()) {
            return -1;
        } else if (this.getType() == t.getType()){
            return 0;
        } else {
            return 1;
        }
    }
}
