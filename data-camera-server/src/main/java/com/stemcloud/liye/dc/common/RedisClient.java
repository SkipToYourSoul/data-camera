package com.stemcloud.liye.dc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Redis 封装
 * Author  : Bean
 * Contact : guhaibin1847@gmail.com
 */
public class RedisClient {

    private static final Logger LOG = LoggerFactory.getLogger(RedisClient.class);
    public static final RedisClient I = new RedisClient();
    private PropKit propKit = PropKit._default();
    private String host = propKit.getString("redis.host");
    private String password = propKit.getString("redis.pwd");
    private Integer port = propKit.getInt("redis.port");
    private Integer index = propKit.getInt("redis.index");

    private JedisPool pool;

    private RedisClient(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(64);
        config.setMaxIdle(64);
        pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT * 3, password);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> pool.close()));
    }

    private <R> R call(Function<Jedis, R> fun, R def){
        try(Jedis jedis = pool.getResource()){
            jedis.select(index);
            return fun.apply(jedis);
        }catch (Exception e){
            LOG.error("redis call error", e);
            // 重要，如果redis不能访问，所以服务全都不能进行，抛出异常
            throw new RuntimeException("redis error", e);
        }
    }

    private void call(Consumer<Jedis> consumer){
        try(Jedis jedis = pool.getResource()){
            consumer.accept(jedis);
        }catch (Exception e){
            LOG.error("redis call error", e);
            // 重要，如果redis不能访问，所以服务全都不能进行，抛出异常
            throw new RuntimeException("redis error", e);
        }
    }

    public void batchCall(Consumer<Pipeline> consumer){
        try(Jedis jedis = pool.getResource()){
            Pipeline pipeline = jedis.pipelined();
            consumer.accept(pipeline);
            pipeline.sync();
        }catch (Exception e){
            LOG.error("redis call error", e);
            // 重要，如果redis不能访问，所以服务全都不能进行，抛出异常
            throw new RuntimeException("redis error", e);
        }
    }

    public void set(String key, String value){
        call(jedis -> jedis.set(key, value));
    }

    public void set(String key, Object value){
        set(key, M_JSON.toJson(value));
    }

    public void del(String key){
        call(jedis -> jedis.del(key));
    }


    public Long incr(String key){
        return call(jedis -> jedis.incr(key), 0L);
    }

    /********************* list ************************/

    public void lpush(String key, Object value){
        call((jedis) -> jedis.lpush(key, M_JSON.toJson(value)));
    }

    public void rpush(String key, Object value){
        call(jedis -> jedis.rpush(key, M_JSON.toJson(value)));
    }

    public Long llen(String key){
        return call((jedis) -> jedis.llen(key), 0L);
    }

    public List<String> lrange(String key, long offset, long limit){
        Long to = 0L;
        if (offset >= 0){
            to = offset + limit - 1;
        }else {
            Long len = llen(key);
            if (len != 0){
                to = (len - 1) + offset + limit;
            }
        }
        Long end = to;
        return call(
                (jedis) -> jedis.lrange(key, offset, end),
                Collections.<String>emptyList()
        );
    }

    public <T> List<T> lrange(Class<T> clazz, String key, long offset, long limit){
        List<String> data = lrange(key, offset, limit);
        return data.stream().map(d -> M_JSON.from(d, clazz)).collect(Collectors.toList());
    }

    public <T> T blpop(Class<T> clazz, String key){
        String json = blpop(key);
        if (json != null){
            return M_JSON.from(json, clazz);
        }else {
            return null;
        }
    }

    public String blpop(String key){
        return call(jedis -> jedis.blpop(new String[]{key}).get(0), null);
    }

    public <T> T brpop(Class<T> clazz, String key){
        String json = brpop(key);
        if (json != null){
            return M_JSON.from(json, clazz);
        }else {
            return null;
        }
    }

    public String brpop(String key){
        return call(jedis -> jedis.brpop(new String[]{key}).get(0), null);
    }

    /**
     * 从列表最右边收缩到指定大小
     * @param key
     * @param size
     */
    public void rtrim(String key, long size){
        if (size < 0){
            return ;
        }
        Long from = -size;
        call((jedis) -> jedis.ltrim(key, from, -1L));
    }

    /****************** map **************************/

    public Long mincr(String key, String subKey){
        return call(jedis -> jedis.hincrBy(key, subKey, 1L), 0L);
    }

    public void mput(String key, String subKey, Object value){
        call((jedis) -> jedis.hset(key, subKey, M_JSON.toJson(value)));
    }

    public void mput(String key, Map<String, Object> map){
        batchCall((pipeline ->
            map.forEach((k, v) ->
                pipeline.hset(key, k, M_JSON.toJson(v))
            )
        ));
    }

    public void mdel(String key, String ... subKeys){
        call((jedis) -> jedis.hdel(key, subKeys));
    }

    public Set<String> mkeys(String key){
        return call((jedis) -> jedis.hkeys(key), Collections.<String>emptySet());
    }

    public Map<String, String> mall(String key){
        return call((jedis) -> jedis.hgetAll(key), Collections.<String, String>emptyMap());
    }

    public <T> Map<String, T> mall(Class<T> clazz, String key){
        return call(jedis -> {
            Map<String, String> map = jedis.hgetAll(key);
            Map<String, T> ret = new HashMap<>(map.size());
            map.forEach((k, v) -> ret.put(k, M_JSON.from(v, clazz)));
            return ret;
        }, Collections.<String, T>emptyMap());
    }
    public <T> List<T> mvalues(final Class<T> clazz, final String key){
        return call(jedis -> {
            Map<String, T> map = mall(clazz, key);
            return new ArrayList<>(map.values());
        }, new LinkedList<T>());
    }

    public boolean mexist(String key, String subKey){
        return call((jedis) -> jedis.hexists(key, subKey), false);
    }

    public List<String> mget(String key, String ... subKeys){
        return call((jedis) -> jedis.hmget(key, subKeys), Collections.<String>emptyList());
    }

    public <T> List<T> mget(Class<T> clazz, String key, String ... subKeys){
        return call(jedis -> {
            List<String> data = jedis.hmget(key, subKeys);
            return data.stream().map(d -> M_JSON.from(d, clazz)).collect(Collectors.toList());
        }, Collections.<T>emptyList());
    }

    public <T> T msingle(Class<T> clazz, String key, String subKey){
        List<T> res = mget(clazz, key, subKey);
        if (res == null || res.isEmpty()){
            return null;
        }
        return res.get(0);
    }

    /****************** set **************************/
    public void sadd(String key, String ... values){
        call((jedis) -> jedis.sadd(key, values));
    }

    public void sdel(String key, String ... values){
        call((jedis) -> jedis.srem(key, values));
    }

    public boolean sexist(String key, String value){
        return call(jedis -> jedis.sismember(key, value), false);
    }

    public Set<String> sall(String key){
        return call(jedis -> jedis.smembers(key), Collections.<String>emptySet());
    }

    public Long slen(String key){
        return call(jedis -> jedis.scard(key), 0L);
    }


    /****************** zset **************************/
    public void zadd(String key, String member, double score){
        call(jedis -> jedis.zadd(key, score, member));
    }

    public Set<String> zrange(String key, double min, double max){
        return call(jedis -> jedis.zrangeByScore(key, min, max), Collections.<String>emptySet());
    }

}
