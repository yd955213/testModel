package com.example.demo.utils;

import com.alibaba.fastjson.TypeReference;
import com.example.demo.entity.api.DeviceHeartBeat;
import com.example.demo.entity.api.Response;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;


/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
class JsonUtilsTest {
    String jsonStr = "{\"Data\":{\"AuthorityCount\":1,\"DevicePort\":8090,\"DeviceTime\":\"2022-02-10 16:57:19\",\"UnuploadRecordsCount\":47},\"DeviceUniqueCode\":\"6D19A9\",\"TimeStamp\":\"2022-02-10 16:57:19\"}";
    @Test
    void toJsonStringWithNull() {
        Response<DeviceHeartBeat> deviceHeartBeatResponse = new Response<>();
        DeviceHeartBeat deviceHeartBeat = new DeviceHeartBeat();
        deviceHeartBeat.setServerTime(MyLocalTimeUtil.getLocalDataTime());
        deviceHeartBeatResponse.setData(deviceHeartBeat);

        Type type = new TypeToken<Response<DeviceHeartBeat>>() {
        }.getType();

        System.out.println(JsonUtils.toJsonStringWithNull(deviceHeartBeatResponse, type));
    }

    @Test
    void toJsonStringNotNull() {
        Response<DeviceHeartBeat> deviceHeartBeatResponse = new Response<>();
        DeviceHeartBeat deviceHeartBeat = new DeviceHeartBeat();
        String time = MyLocalTimeUtil.getLocalDataTime();
        deviceHeartBeat.setServerTime(time);
        deviceHeartBeatResponse.setData(deviceHeartBeat);

        Type type = new TypeToken<Response<DeviceHeartBeat>>() {
        }.getType();

        System.out.println(JsonUtils.toJsonStringNotNull(deviceHeartBeatResponse, type));

        Response<DeviceHeartBeat> response = new Response<>();
        deviceHeartBeat = new DeviceHeartBeat();
        deviceHeartBeat.setServerTime(time);
//        response.setTimeStamp(time);
        response.setData(deviceHeartBeat);

        String s = JsonUtils.toJsonStringNotNull(response, new TypeToken<Response<DeviceHeartBeat>>() {
        }.getType());

        System.out.println(s);
    }

    @Test
    void parseObject() {
        Type type = new TypeReference<Response<DeviceHeartBeat>>() {
        }.getType();
        System.out.println(JsonUtils.parseObject(jsonStr, type).toString());
    }
}