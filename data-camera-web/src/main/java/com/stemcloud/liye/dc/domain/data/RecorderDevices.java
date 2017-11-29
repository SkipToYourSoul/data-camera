package com.stemcloud.liye.dc.domain.data;

import java.util.List;

/**
 * Belongs to debug
 * Description:
 *
 * @author liye on 2017/11/29
 */
public class RecorderDevices {
    private List<Long> sensors;
    private List<Long> tracks;

    public List<Long> getSensors() {
        return sensors;
    }

    public void setSensors(List<Long> sensors) {
        this.sensors = sensors;
    }

    public List<Long> getTracks() {
        return tracks;
    }

    public void setTracks(List<Long> tracks) {
        this.tracks = tracks;
    }
}
