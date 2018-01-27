package com.stemcloud.liye.dc.domain.data;

import java.util.List;

/**
 * Belongs to debug
 * Description:
 *
 * @author liye on 2017/11/29
 */
public class RecorderDevices {
    private Long sensor;
    private Long track;
    private List<String> legends;

    public Long getSensor() {
        return sensor;
    }

    public void setSensor(Long sensor) {
        this.sensor = sensor;
    }

    public Long getTrack() {
        return track;
    }

    public void setTrack(Long track) {
        this.track = track;
    }

    public List<String> getLegends() {
        return legends;
    }

    public void setLegends(List<String> legends) {
        this.legends = legends;
    }
}
