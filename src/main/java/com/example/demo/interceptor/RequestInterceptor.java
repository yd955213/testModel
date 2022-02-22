package com.example.demo.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.demo.entity.api.DeviceHeartBeat;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.base.DeviceInfo;
import com.example.demo.service.init.DeviceInfoMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description: HTTP请求拦截器
 * @modifiedBy:
 */
@Controller
@Log4j2
public class RequestInterceptor implements HandlerInterceptor {

    private final Map<String, DeviceInfo> deviceInfoMap = DeviceInfoMap.getDeviceInfoMap();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        Integer port = request.getRemotePort();
        log.info("请求IP:{}、端口：{}", ip, port);

        //获取请求参数
        String queryString = request.getRequestURI();
        log.info("请求地址:{}", queryString);


        //获取请求body
        byte[] bodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
        String body = new String(bodyBytes, request.getCharacterEncoding());
        // 记录上报数据量过大，不打印日志
        if(!"/ServerApi/UploadRecords".equals(queryString))
            log.info("请求体：{}", body);

        if("/ServerApi/DeviceHeartBeat".equals(queryString)){
            Request<DeviceHeartBeat> requestJson = JSONObject.parseObject(body, new TypeReference<Request<DeviceHeartBeat>>(){}.getType());
            if (requestJson != null){
                // 如果Ip 和端口变话 则修改
                if (deviceInfoMap.containsKey(requestJson.getDeviceUniqueCode())){
                    if(!ip.equals(deviceInfoMap.get(requestJson.getDeviceUniqueCode()).getDeviceIp())){
                        deviceInfoMap.get(requestJson.getDeviceUniqueCode()).setDeviceIp(ip);
                    }
                    if(!requestJson.getData().getDevicePort().equals(deviceInfoMap.get(requestJson.getDeviceUniqueCode()).getDevicePort())){
                        deviceInfoMap.get(requestJson.getDeviceUniqueCode()).setDevicePort(requestJson.getData().getDevicePort());
                    }
                }else {
                    // 添加设备 所有信息
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setDeviceUniqueCode(requestJson.getDeviceUniqueCode());
                    deviceInfo.setDeviceIp(ip);
                    deviceInfo.setDevicePort(requestJson.getData().getDevicePort());
                    deviceInfoMap.put(deviceInfo.getDeviceUniqueCode(), deviceInfo);
                }
            }
        }
//        return HandlerInterceptor.super.preHandle(request, response, handler);
        return true;
    }
}
