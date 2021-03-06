package com.example.demo.utils.restTemplateUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 封装RestTemple 请求的消息头
 * @modifiedBy:
 */
@Controller
public class HttpHeadersUtil {
    private static HttpHeaders headers;
    public static void init(){
        headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
    }

    public static HttpHeaders getHeaders() {
        if(headers == null){
            init();
        }
        return headers;
    }


}
