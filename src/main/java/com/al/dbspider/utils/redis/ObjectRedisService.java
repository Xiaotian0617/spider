package com.al.dbspider.utils.redis;


import java.util.Map;

public interface ObjectRedisService {

    void set(String key, Object obj);


    /**
     * Reset the cache k v with the same expire.
     *
     * @param key key
     * @param obj value
     */
    void reSet(String key, Object obj);

    /**
     * Set kv with expire
     *
     * @param key    k
     * @param obj    v
     * @param expire expire
     */
    public void set(String key, Object obj, Long expire);

    void remove(String key);

    Object get(String key);

    void setHashModel(String key, String HK, Object HV);

    void setHashModel(String key, String HK);

    void setHashMap(String key, Map<String, Object> map);

    void deleteHashModel(String key, String price);

    Object getHashModel(String key, String hk);

    Object getHashModel(String key);

    Object getEntries(String key);

    Boolean hasKey(String key, String price);


}


