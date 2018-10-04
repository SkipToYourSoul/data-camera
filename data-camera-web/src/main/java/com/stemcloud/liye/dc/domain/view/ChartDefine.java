package com.stemcloud.liye.dc.domain.view;

import com.stemcloud.liye.dc.domain.data.UserDefineChart;

import java.util.ArrayList;
import java.util.List;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/10/3
 */
public class ChartDefine {
    private UserDefineChart info;
    private List<List<Double>> data;
    private List<String> timestamp;
    private Double xMin;
    private Double xMax;
    private Double yMin;
    private Double yMax;

    public UserDefineChart getInfo() {
        return info;
    }

    public void setInfo(UserDefineChart info) {
        this.info = info;
    }

    public List<List<Double>> getData() {
        return data;
    }

    public void setData(List<ChartTimeSeries> x, List<ChartTimeSeries> y) {
        List<List<Double>> result = new ArrayList<List<Double>>();
        List<String> timestamp = new ArrayList<String>();
        for (int index = 0; index < x.size(); index++) {
            if (x.get(index).getValue().size() == 2 && y.get(index).getValue().size() == 2) {
                List<Double> list = new ArrayList<Double>();
                double xv = Double.valueOf(String.valueOf(x.get(index).getValue().get(1)));
                double yv = Double.valueOf(String.valueOf(y.get(index).getValue().get(1)));
                String time = String.valueOf(x.get(index).getValue().get(0));
                timestamp.add(time);
                if (xMin == null || xv < xMin) {
                    xMin = xv;
                }
                if (xMax == null || xv > xMax) {
                    xMax = xv;
                }
                if (yMin == null || yv < yMin) {
                    yMin = yv;
                }
                if (yMax == null || yv > yMax) {
                    yMax = yv;
                }

                list.add(xv);
                list.add(yv);
                result.add(list);
            }
        }
        this.xMax = Math.ceil(xMax);
        this.xMin = Math.floor(xMin);
        this.yMax = Math.ceil(yMax);
        this.yMin = Math.floor(yMin);
        this.timestamp = timestamp;
        this.data = result;
    }

    public Double getxMin() {
        return xMin;
    }

    public Double getxMax() {
        return xMax;
    }

    public Double getyMin() {
        return yMin;
    }

    public Double getyMax() {
        return yMax;
    }

    public List<String> getTimestamp() {
        return timestamp;
    }
}
