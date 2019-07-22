package com.stemcloud.liye.dc.domain.data;

import com.google.gson.Gson;

import java.util.ArrayList;
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

    public RecorderDevices(Long sensor, Long track, List<String> legends) {
        this.sensor = sensor;
        this.track = track;
        this.legends = legends;
    }

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

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("温度");
        list.add("湿度");
        RecorderDevices devices = new RecorderDevices((long)3, (long)2, list);
        System.out.println(new Gson().toJson(devices));
    }
}
