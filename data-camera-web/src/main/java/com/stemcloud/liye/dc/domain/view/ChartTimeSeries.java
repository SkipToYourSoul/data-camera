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
    private Map<String, Object> itemStyle;

    private Map<String, Map<String, Object>> emphasis;

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
        Map<String, Object> itemStyle = new HashMap<String, Object>();

        if (vd.getMark() != null && !vd.getMark().trim().isEmpty()){
            this.name = vd.getMark();
        }else {
            this.name = "无";
        }
        itemStyle.put("id", vd.getId());
        this.itemStyle = itemStyle;

        // emphasis map
        Map<String, Map<String, Object>> emphasis = new HashMap<String, Map<String, Object>>();
        Map<String, Object> eItemStyle = new HashMap<String, Object>();
        if (vd.getMark() != null && !vd.getMark().trim().isEmpty()){
            eItemStyle.put("color", "#fffff");
            eItemStyle.put("borderColor", "#00a6db");
            eItemStyle.put("borderWidth", 5);
            eItemStyle.put("opacity", 0.8);
        }
        emphasis.put("itemStyle", eItemStyle);
        this.emphasis = emphasis;
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

    public Map<String, Object> getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(Map<String, Object> itemStyle) {
        this.itemStyle = itemStyle;
    }

    public Map<String, Map<String, Object>> getEmphasis() {
        return emphasis;
    }

    public void setEmphasis(Map<String, Map<String, Object>> emphasis) {
        this.emphasis = emphasis;
    }
}
