package com.example.demo.entity.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.bytebuddy.description.method.MethodDescription;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
class DeviceHeartBeatTest {

    @Test
    void getDeviceTime() {
        String st = "{\"Data\":{\"AuthorityCount\":1,\"DevicePort\":8090,\"DeviceTime\":\"2022-02-10 13:31:59\",\"UnuploadRecordsCount\":47},\"DeviceUniqueCode\":\"6D19A9\",\"TimeStamp\":\"2022-02-10 13:31:59\"}";
        Type type = new TypeToken<Request<DeviceHeartBeat>>() {
        }.getType();
        System.out.println(new Gson().fromJson(st, type).toString());


        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        System.out.println(now);
        System.out.println(now.format(dataTimeFormatter));
    }
}