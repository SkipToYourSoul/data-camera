package com.stemcloud.liye.dc.domain;

/**
 * Belongs to data-camera-server
 * Description:
 *
 * @author liye on 2018/1/15.
 */
public class SensorStatus {
    private String code;
    private int isMonitor;
    private long id;
    private long trackId;
    private long sensorConfigId;

    public SensorStatus(String code, int isMonitor, long id, long trackId, long sensorConfigId) {
        this.code = code;
        this.isMonitor = isMonitor;
        this.id = id;
        this.trackId = trackId;
        this.sensorConfigId = sensorConfigId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getIsMonitor() {
        return isMonitor;
    }

    public void setIsMonitor(int isMonitor) {
        this.isMonitor = isMonitor;
    }

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

    public long getSensorConfigId() {
        return sensorConfigId;
    }

    public void setSensorConfigId(long sensorConfigId) {
        this.sensorConfigId = sensorConfigId;
    }
}
