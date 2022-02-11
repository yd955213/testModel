package com.example.demo.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description: 获取当前系统时间
 * @modifiedBy:
 */
public class MyLocalTimeUtil {
    /**
     * 时间格式：yyyy-MM-dd hh:mm:ss
     * @return yyyy-MM-dd hh:mm:ss
     */
    public static String  getLocalDataTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        return now.format(dataTimeFormatter);
    }
}
