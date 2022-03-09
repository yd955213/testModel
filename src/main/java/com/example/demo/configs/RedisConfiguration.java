//package com.example.demo.configs;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///**
// * @author: yd
// * @date: 2022-02-22
// * @version: 1.0
// * @description: 解决redis 存储中文 问题
// * @modifiedBy:
// */
//@Configuration
//public class RedisConfiguration {
//    @Autowired
//    RedisTemplate<String, Object> redisTemplate;
//    @Bean
//    public RedisTemplate<String, Object> getRedisTemplate(){
//        StringRedisSerializer redisSerializer = new StringRedisSerializer();
//        redisTemplate.setKeySerializer(redisSerializer);
//        redisTemplate.setValueSerializer(redisSerializer);
//        redisTemplate.setDefaultSerializer(redisSerializer);
//        redisTemplate.setHashKeySerializer(redisSerializer);
//        redisTemplate.setHashValueSerializer(redisSerializer);
//        redisTemplate.setStringSerializer(redisSerializer);
//        return redisTemplate;
//    }
//}
