package com.stemcloud.liye.dc.domain.data;

import javax.persistence.*;
import java.util.Date;

/**
 * Belongs to data-camera-web
 * Description:
 *  dc_data_video_data
 * @author liye on 2017/11/29
 */
@Entity
@Table(name = "dc_data_video_data")
public class VideoData {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "track_id", nullable = false)
    private long trackId;

    @Column(name = "sensor_id", nullable = false)
    private long sensorId;

    @OneToOne
    private RecorderInfo recorderInfo;

    @Column(name = "video_path")
    private String videoPath;

    public long getId() {
        return id;
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

    public RecorderInfo getRecorderInfo() {
        return recorderInfo;
    }

    public void setRecorderInfo(RecorderInfo recorderInfo) {
        this.recorderInfo = recorderInfo;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
