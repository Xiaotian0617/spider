package com.al.dbspider.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * JSON转换工具类 使用前调用getInstance方法
 */
public class GsonUtil {


    private GsonUtil() {
    }

    private static class gsonUtilHolder {
        private static GsonUtil instance = new GsonUtil();
    }

    /**
     * 使用方法前调用getInstance,获得gsonUtil实例
     *
     * @return
     */
    public static GsonUtil getInstance() {
        return gsonUtilHolder.instance;
    }

    /**
     * 将对象转换成json字符串
     *
     * @param obj
     * @return
     */
    public String toJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue
                , SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty);
    }

    /**
     * 在json字符串中，根据key值找到value
     *
     * @param data
     * @param key
     * @return
     */

    public <T> T getValue(String data, String key, Class<T> cls) {
        JSONObject jsonObject = JSON.parseObject(data);
        return jsonObject.getObject(key, cls);
    }

    /**
     * 获取key值,默认String
     *
     * @param data
     * @param key
     * @return
     */
    public String getValue(String data, String key) {

        return getValue(data, key, String.class);
    }

    /**
     * 将json格式转换成map对象
     *
     * @param json
     * @return
     */
    public <T> T toMap(String json, TypeReference<T> reference) {
        String string = toJson(JSON.parseObject(json, reference));

        return JSON.parseObject(string, reference);
    }

    /**
     * obj 转为 map
     *
     * @param obj 需要转的对象
     * @return
     */
    public <T> T objToMap(Object obj, TypeReference<T> reference) {
        if (obj != null) {
            return toMap(toJson(obj), reference);
        }
        return null;
    }

    /**
     * 将json转换成bean对象
     *
     * @param <T>
     * @param json
     * @param clazz
     * @return
     */
    public <T> T toObj(String json, Class<T> clazz) {
        String string = toJson(JSON.parseObject(json, clazz));//转换一遍  保证空值
        return JSON.parseObject(string, clazz);
    }

    public <T> T toObj(String json, String key, Class<T> clazz) {
        String value = getValue(json, key, String.class);
        return toObj(value, clazz);
    }

    /**
     * 将json格式转换成List对象
     * <p>
     *
     * @param json
     * @return
     */
    public <T> List<T> toList(String json, Class<T> cls) {
        String string = toJson(JSON.parseArray(json, cls));//转换一遍  保证空值
        return JSON.parseArray(string, cls);
    }

    /**
     * @param json
     * @param key
     * @param cls
     * @param <T>
     * @return
     */
    public <T> List<T> toList(String json, String key, Class<T> cls) {
        String value = getValue(json, key, String.class);
        return toList(value, cls);
    }


}
