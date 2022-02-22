package com.example.demo.utils.restTemplateUtil;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 合成url
 * @modifiedBy:
 */
public class UrlUtil {
    public static String getUrl(String ip, Object port, String uri){
        return "http://" + ip + ":" + port.toString() + uri;
    }
}
