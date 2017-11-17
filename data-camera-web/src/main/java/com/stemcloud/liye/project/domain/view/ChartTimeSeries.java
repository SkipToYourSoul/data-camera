package com.stemcloud.liye.project.domain.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *  time series for line chart
 * @author liye on 2017/11/17
 */
public class ChartTimeSeries {
    private Date name;
    private List<Object> value;

    public ChartTimeSeries(Date date, String value) {
        List<Object> list = new ArrayList<Object>();
        list.add(date);
        list.add(value);
        this.value = list;
        this.name = date;
    }

    public Date getName() {
        return name;
    }

    public void setName(Date name) {
        this.name = name;
    }

    public List<Object> getValue() {
        return value;
    }

    public void setValue(List<Object> value) {
        this.value = value;
    }
}
