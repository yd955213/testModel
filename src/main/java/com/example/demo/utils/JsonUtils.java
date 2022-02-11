package com.example.demo.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Type;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description: 使用gson序列化bean, fastjson反序列化json字符串
 * @modifiedBy:
 */
@Log4j2
public class JsonUtils {

    /**
     * 序列化json, 可序列化null属性
     * @param bean bean对象
     * @param typeToken Type type = new TypeToken<>() {}.getType(); com.google.gson.reflect.TypeToken
     * @return json字符串
     */
    public static String toJsonStringWithNull(Object bean, Type typeToken){
//        Type type = new TypeToken<>() {}.getType();
        Gson gson = new GsonBuilder().serializeNulls().create();
        return toJsonString(gson, bean, typeToken);
    }

    /**
     * 序列化json
     * @param bean bean对象
     * @param typeToken Type type = new TypeToken<>() {}.getType();  com.google.gson.reflect.TypeToken
     * @return json字符串
     */
    public static String toJsonStringNotNull(Object bean, Type typeToken){
//        Type type = new TypeToken<>() {}.getType();
        Gson gson = new Gson();
        return toJsonString(gson, bean, typeToken);
    }


    /**
     * 反序列化json  人脸照片
     * @param jsonStr json 字符串
     * @param typeReference Type type = new TypeReference<T>(){}.getType();  com.alibaba.fastjson.TypeReference
     * @param <T> T
     * @return T
     */
    public static <T> T parseObject(String jsonStr, Type typeReference){
//        Type type = new TypeReference<T>(){}.getType();
        try {
            return (T) JSONObject.parseObject(jsonStr, typeReference);
        }catch (Exception e){
            log.error("反序列化json失败：json字符串：{}，错误信息：{}", jsonStr, e.getMessage());
            return null;
        }
    }

    private static String toJsonString(Gson gson, Object bean, Type typeToken){
        String s = null;
        try {
            s = gson.toJson(bean, typeToken);
        }catch (Exception e){
//            e.printStackTrace();
            log.error("序列化json失败：bean对象：{}，错误信息：{}", bean, e.getMessage());
        }
        return s;
    }
}
