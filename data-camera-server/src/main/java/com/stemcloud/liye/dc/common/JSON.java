package com.stemcloud.liye.dc.common;

/**
 * Project : content-index-realtime-etl
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public final class JSON {

    public static String toJson(Object obj){
        return com.alibaba.fastjson.JSON.toJSONString(obj);
    }

    public static <T> T from(String json, Class<T> clazz){
        return com.alibaba.fastjson.JSON.parseObject(json, clazz);
    }

}
