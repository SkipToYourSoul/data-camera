package com.stemcloud.liye.dc.domain.data;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_data_recorder_info
 * @author liye on 2017/11/22
 */
@Entity
@Table(name = "dc_data_recorder_info")
public class RecorderInfo {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "exp_id", nullable = false)
    private long expId;

    @Column(name = "track_ids", nullable = false)
    private String trackIds;

    @Column(name = "sensor_ids", nullable = false)
    private String sensorIds;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "is_recorder",
            columnDefinition = "INT DEFAULT 1")
    private int isRecorder = 1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExpId() {
        return expId;
    }

    public void setExpId(long expId) {
        this.expId = expId;
    }

    public String getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(String trackIds) {
        this.trackIds = trackIds;
    }

    public String getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(String sensorIds) {
        this.sensorIds = sensorIds;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getIsRecorder() {
        return isRecorder;
    }

    public void setIsRecorder(int isRecorder) {
        this.isRecorder = isRecorder;
    }
}
