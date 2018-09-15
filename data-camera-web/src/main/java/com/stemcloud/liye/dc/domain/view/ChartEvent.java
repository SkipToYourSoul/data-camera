package com.stemcloud.liye.dc.domain.view;

/**
 * Belongs to data-camera-web
 * Description:
 *  图表中标注的事件
 * @author liye on 2018/9/15
 */
public class ChartEvent {
    private long time;
    private String mark;
    private String legend;

    public ChartEvent(long time, String mark, String legend) {
        this.time = time;
        this.mark = mark;
        this.legend = legend;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }
}
