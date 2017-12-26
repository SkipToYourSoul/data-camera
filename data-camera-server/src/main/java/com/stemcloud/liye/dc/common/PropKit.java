package com.stemcloud.liye.dc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Properties Tool
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public final class PropKit {

    private static final Logger LOG = LoggerFactory.getLogger(PropKit.class);
    private static final Map<String, PropKit> CACHE = new ConcurrentHashMap<>();

    private final Properties prop;

    private PropKit(Properties prop){
        this.prop = prop;
    }

    public static PropKit _default(){
        return use("server.properties");
    }

    public static PropKit use(String name){
        if (!CACHE.containsKey(name)){
            synchronized (PropKit.class) {
                if (!CACHE.containsKey(name)) {
                    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
                    Properties properties = new Properties();
                    try {
                        properties.load(in);
                        CACHE.put(name, new PropKit(properties));
                    } catch (IOException e) {
                        throw new RuntimeException("load properties error", e);
                    }
                }
            }
        }
        return CACHE.get(name);
    }

    public String getString(String key){
        return prop.getProperty(key);
    }

    public Integer getInt(String key){
        return Integer.parseInt(getString(key));
    }

    public Long getLong(String key){
        return Long.parseLong(getString(key));
    }


}
