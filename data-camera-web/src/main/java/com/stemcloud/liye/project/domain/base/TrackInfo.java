package com.stemcloud.liye.project.domain.base;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to smart-sensor
 * Author: liye on 2017/10/31
 * Description:
 * @author liye
 */
@Entity
@Table(name = "dc_base_track_info")
public class TrackInfo {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private ExperimentInfo experiment;

    @OneToOne
    private SensorInfo sensor;

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

    @JsonBackReference
    public void setExperiment(ExperimentInfo experiment) {
        this.experiment = experiment;
    }

    public SensorInfo getSensor() {
        return sensor;
    }

    public void setSensor(SensorInfo sensor) {
        this.sensor = sensor;
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
