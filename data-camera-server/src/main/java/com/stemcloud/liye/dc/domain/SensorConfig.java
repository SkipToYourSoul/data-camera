package com.stemcloud.liye.dc.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Belongs to data-camera-server
 * Description:
 *  sensor config
 * @author liye on 2017/11/21
 */
public class SensorConfig {
    private long id;
    private List<String> dimension;
    private int type;

    public SensorConfig(long id, String dimension, int type) {
        this.id = id;
        this.type = type;
        this.dimension = Arrays.asList(dimension.split(";"));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getDimension() {
        return dimension;
    }

    public void setDimension(List<String> dimension) {
        this.dimension = dimension;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
