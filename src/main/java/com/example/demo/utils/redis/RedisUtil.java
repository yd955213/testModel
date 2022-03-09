package com.example.demo.utils.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: yd
 * @date: 2022-02-28
 * @version: 1.0
 * @description: redis 进行读写
 * @modifiedBy:
 */
@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private BoundValueOperations<String, String> stringKey;
    private BoundSetOperations<String, String> setKey;

    /**
     * 部分 redis 的key 需要替换 如:personInfo:isDown:{设备mac}:{uniqueCode}
     * 改为personInfo:isDown:设备mac:uniqueCode
     *
     * @param keys keys
     * @return keys
     */
    public String getKey(String... keys) {
        StringBuilder key = new StringBuilder();
        for (String s : keys) {
            key.append(s).append(":");
        }
        key = new StringBuilder("".equals(key.toString()) ? key.toString() : key.substring(0, key.length() - 1));
        return key.toString();
    }

    public void deleteRedisKey(String key){
        stringRedisTemplate.unlink(key);
    }

    public void increment(String key) {
        stringKey = stringRedisTemplate.boundValueOps(key);
        stringKey.increment();
    }

    /**
     * redis 对应键值 increment为 正整数 未自增， 为负整数为自减
     *
     * @param key       key
     * @param increment 增量 可正可负
     */
    public void increment(String key, long increment) {
        stringKey = stringRedisTemplate.boundValueOps(key);
        stringKey.increment(increment);
    }

    public void stringSet(String key, String value) {
        stringKey = stringRedisTemplate.boundValueOps(key);
        stringKey.set(value);
    }


    public String stringGet(String key) {
        stringKey = stringRedisTemplate.boundValueOps(key);
        return stringKey.get();
    }

    public void setAndExpireByString(String key, String value, long expire) {
        if (!hasKey(key)) stringSet(key, value);
        expireByString(key, expire);
    }

    /**
     * 设置过期时间
     *
     * @param key    键
     * @param expire 时长， 单位默认 秒
     */
    public void expireByString(String key, long expire) {
        stringKey = stringRedisTemplate.boundValueOps(key);
        stringKey.expire(expire, TimeUnit.SECONDS);
    }

    public void setAdd(String key, String... value) {
        setKey = stringRedisTemplate.boundSetOps(key);
        setKey.add(value);
    }

    public void setRemove(String key, Object... value) {
        setKey = stringRedisTemplate.boundSetOps(key);
        setKey.remove(value);
    }

    public Set<String> setGet(String key) {
        setKey = stringRedisTemplate.boundSetOps(key);
        return setKey.members();
    }
    public int getSetSize(String key) {
        setKey = stringRedisTemplate.boundSetOps(key);
        Long size = setKey.size();
        return ObjectUtils.isEmpty(size) ? 0 : size.intValue();
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public boolean hasValueInSet(String key, String value) {
        if (!hasKey(key)) return false;
        setKey = stringRedisTemplate.boundSetOps(key);
        return Boolean.TRUE.equals(setKey.isMember(value));
    }
}
