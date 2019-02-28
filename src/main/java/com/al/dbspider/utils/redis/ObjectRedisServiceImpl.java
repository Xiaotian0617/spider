package com.al.dbspider.utils.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository("objectRedisService")
public class ObjectRedisServiceImpl implements ObjectRedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object obj) {
        redisTemplate.opsForValue().set(key, obj);
    }


    /**
     * Reset the cache k v with the same expire.
     *
     * @param key key
     * @param obj value
     */
    public void reSet(String key, Object obj) {
        Long expire = redisTemplate.getExpire(key);
        redisTemplate.opsForValue().set(key, obj);
        if (expire != null && expire.intValue() > 0) {
            this.redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }

    /**
     * Set kv with expire
     *
     * @param key    k
     * @param obj    v
     * @param expire expire
     */
    public void set(String key, Object obj, Long expire) {
        redisTemplate.opsForValue().set(key, obj);
        if (expire != null && expire.intValue() > 0) {
            this.redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }

    public void remove(String key) {
        redisTemplate.delete(key);
    }

    public Object get(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        return obj;
    }

    public void setHashModel(String key, String HK, Object HV) {
        redisTemplate.opsForHash().put(key, HK, HV);
    }

    public void setHashModel(String key, String HK) {
        setHashModel(key, HK, HK);
    }

    public void setHashMap(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public void deleteHashModel(String key, String price) {
        redisTemplate.opsForHash().delete(key, price);
    }


    @Override
    public Object getHashModel(String key, String hk) {
        Object obj = redisTemplate.opsForHash().get(key, hk);
        return obj;
    }

    @Override
    public Object getHashModel(String key) {
        return null;
    }

    public Object getEntries(String key) {
        Object obj = redisTemplate.opsForHash().entries(key);
        return obj;
    }

    public Boolean hasKey(String key, String price) {
        return redisTemplate.opsForHash().hasKey(key, price);
    }


}
