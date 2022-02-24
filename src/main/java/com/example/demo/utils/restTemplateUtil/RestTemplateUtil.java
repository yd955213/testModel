package com.example.demo.utils.restTemplateUtil;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description: 封装HTTP请求
 * @modifiedBy:
 */
@Component
@Log4j2
public class RestTemplateUtil {
    @Autowired
    RestTemplate restTemplate;

    /**
     * 带参数的 get 请求
     * @param url 请求地址
     * @param mediaType  ContentType
     * @param multiValueMap 参数
     * @return 响应体
     */
    public String get(String url, MediaType mediaType, MultiValueMap<String, String> multiValueMap){
        // 设置请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(mediaType);
        // 拼接URl
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.queryParams(multiValueMap).build().encode().toUri();

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
            log.info("get请求：url:{}", uri);
            String body = responseEntity.getBody();
            log.info("get请求成功：url:{} ,返回信息：{}", uri, body);
            return body;
        }catch (Exception e){
            // 自定义请求失败的参数并返回  现在直接返回null
            log.info("get请求失败：url:{}，异常信息：{}", uri, e.getMessage());
            return null;
        }
    }

    /**
     * post请求 json格式
     * @param url url
     * @param requestParamJson json字符串
     * @return String
     */
    public String post(String url, String requestParamJson){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        HttpEntity<String> httpEntity = new HttpEntity<>(requestParamJson, httpHeaders);
        ResponseEntity<String> responseEntity;
        try {
            log.info("post请求：url:{}, 参数：{}", url, requestParamJson);
            responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            log.info("post请求成功：url:{} ,返回信息：{}", url, responseEntity.getBody());
            return responseEntity.getBody();
        }catch (Exception e){
            log.info("post请求失败：url:{}，异常信息：{}", url, e.getMessage());
            return null;
        }
    }
    public String post(URI url, String requestParamJson){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        HttpEntity<String> httpEntity = new HttpEntity<>(requestParamJson, httpHeaders);
        ResponseEntity<String> responseEntity;
        try {
            log.info("post请求：url:{}, 参数：{}", url, requestParamJson);
            responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            log.info("post请求成功：url:{} ,返回信息：{}", url, responseEntity.getBody());
            return responseEntity.getBody();
        }catch (Exception e){
            log.info("post请求失败：url:{}，异常信息：{}", url, e.getMessage());
            return null;
        }
    }

    public String get(String url){

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(url, String.class);
            log.info("get请求：url:{}", url);
            String body = responseEntity.getBody();
            log.info("get请求成功：url:{} ,返回信息：{}", url, body);
            return body;
        }catch (Exception e){
            // 自定义请求失败的参数并返回  现在直接返回null
            log.info("get请求失败：url:{}，异常信息：{}", url, e.getMessage());
            return null;
        }
    }
    public ResponseEntity<FileSystemResource> upload(String filePath){
        File file = new File(filePath);
//        if(file.exists()){
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Disposition",  "attachment;fileName=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("uploadFile", fileSystemResource);

            log.info("开始进行文件上传");
//        }
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileSystemResource);
    }
    public String downLoad(String url){

        return null;
    }
}
