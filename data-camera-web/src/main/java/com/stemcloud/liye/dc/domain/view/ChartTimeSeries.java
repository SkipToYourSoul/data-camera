package com.stemcloud.liye.dc.domain.view;

import com.stemcloud.liye.dc.domain.data.ValueData;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  time series for line chart
 * @author liye on 2017/11/17
 */
public class ChartTimeSeries {
    /** data mark **/
    private String name;

    /** data value **/
    private List<Object> value;

    /** data style **/
    private Map<String, Map<String, Object>> itemStyle;

    public ChartTimeSeries(Date date){
        List<Object> list = new ArrayList<Object>();
        list.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date));
        this.value = list;
    }

    public ChartTimeSeries(ValueData vd){
        // value
        List<Object> list = new ArrayList<Object>();
        list.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(vd.getCreateTime()));
        list.add(vd.getValue());
        this.value = list;

        // mark and style
        if (vd.getMark() != null){
            this.name = vd.getMark();
            Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
            Map<String, Object> subMap = new HashMap<String, Object>();
            subMap.put("color", "red");
            subMap.put("borderColor", "red");
            subMap.put("borderWidth", 10);
            subMap.put("borderType", "dotted");
            subMap.put("opacity", 0.7);
            map.put("normal", subMap);
            this.itemStyle = map;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getValue() {
        return value;
    }

    public void setValue(List<Object> value) {
        this.value = value;
    }

    public Map<String, Map<String, Object>> getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(Map<String, Map<String, Object>> itemStyle) {
        this.itemStyle = itemStyle;
    }
}
