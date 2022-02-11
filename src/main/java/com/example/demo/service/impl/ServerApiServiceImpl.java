package com.example.demo.service.impl;

import com.example.demo.entity.api.*;
import com.example.demo.entity.base.DeviceInfo;
import com.example.demo.interceptor.RequestInterceptor;
import com.example.demo.service.ServerApiService;
import com.example.demo.service.init.DeviceInfoMap;
import com.example.demo.service.init.FaceDeviceApi;
import com.example.demo.service.init.PersonInfo;
import com.example.demo.utils.HttpHeadersUtil;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.UrlUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */

@Service("devicesHeartBeatService")
@Log4j2
public class ServerApiServiceImpl implements ServerApiService {

    @Override
    public String save(Request<DeviceHeartBeat> DeviceHeartBeatRequest) {
        log.info("心跳上报接口， 当前设备信息{}", RequestInterceptor.getDeviceInfoMap().get(DeviceHeartBeatRequest.getDeviceUniqueCode()));
        String time = MyLocalTimeUtil.getLocalDataTime();

        Response<DeviceHeartBeat> response = new Response<>();
        DeviceHeartBeat deviceHeartBeat = new DeviceHeartBeat();
        deviceHeartBeat.setServerTime(time);
        response.setTimeStamp(time);
        response.setData(deviceHeartBeat);

        String s = JsonUtils.toJsonStringNotNull(response, new TypeToken<Response<DeviceHeartBeat>>() {
        }.getType());
        log.info("心跳上报接口， 返回数据：{}", s);
        // 项目初期，未使用数据库 这里 isDownloadMap 进行赋值
        DeviceInfoMap.initDownloadMap(DeviceHeartBeatRequest.getDeviceUniqueCode());
        return s;
    }

    @Autowired
    RestTemplate restTemplate;
    @Override
    public String noticeOfDownloadAuthorityData(Request<NoticeOfDownloadAuthorityData> noticeOfDownloadAuthorityDataRequest) {
        String isReady = noticeOfDownloadAuthorityDataRequest.getData().getIsReady();
        String deviceUniqueCode = noticeOfDownloadAuthorityDataRequest.getDeviceUniqueCode();
        /**
         * 进行 人员信息初始化
         */
        if(PersonInfo.getPersonInfoMap().isEmpty()){
            new PersonInfo().init();
        }
        if("Y".equals(isReady)){
            if (DeviceInfoMap.getIsDownloadMap().get(deviceUniqueCode)){
                new Thread(()->{
                    DeviceInfo deviceInfo = RequestInterceptor.getDeviceInfoMap().get(deviceUniqueCode);

                    Request<List<DownloadAuthorityData>> downloadAuthorityDataRequest = new Request<>();
                    downloadAuthorityDataRequest.setTimeStamp(MyLocalTimeUtil.getLocalDataTime());
                    downloadAuthorityDataRequest.setDeviceUniqueCode(deviceUniqueCode);

                    List<DownloadAuthorityData> downloadAuthorityDataList = new ArrayList<>();
                    PersonInfo.getPersonInfoMap().keySet().forEach(key -> downloadAuthorityDataList.add(PersonInfo.getPersonInfoMap().get(key)));

                    downloadAuthorityDataRequest.setData(downloadAuthorityDataList);

                    String requestParam = JsonUtils.toJsonStringNotNull(downloadAuthorityDataRequest, new TypeToken<Request<List<DownloadAuthorityData>>>() {
                    }.getType());


                    HttpHeaders headers = HttpHeadersUtil.getHeaders();
                    HttpEntity<String> httpEntity = new HttpEntity<>(requestParam, headers);
                    String url = UrlUtil.getUrl(deviceInfo.getDeviceIp(), deviceInfo.getDevicePort(), FaceDeviceApi.downloadAuthorityData);
                    // 进行权限下发操作
                    log.info("下载人员身份数据：Url={}, 数据长度：{}", url, requestParam.length());
                    ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
                    if(responseEntity != null) {
                        log.info("设备返回：{}", responseEntity.getBody());
                    }

                }).start();

                // 暂时不考虑是否下载成功，将下载标志位置为false
                DeviceInfoMap.getIsDownloadMap().put(deviceUniqueCode, false);
            }
        }

        Response<String> response = new Response<>();

        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }

    @Override
    public String uploadAuthorityDealResult(Request<UploadAuthorityDealResult> uploadAuthorityDealResultRequest) {
        String deviceUniqueCode = uploadAuthorityDealResultRequest.getDeviceUniqueCode();
        String code = uploadAuthorityDealResultRequest.getData().getCode();
        String msg = uploadAuthorityDealResultRequest.getData().getMsg();
        if("0".equals(code)){
            log.info("设备：{},处理权限数据成功，处理结果：{}", deviceUniqueCode, msg);
        }else {
            log.info("设备：{},处理权限数据失败！，处理结果{}，失败信息：{}", deviceUniqueCode, code, msg);
        }
        return JsonUtils.toJsonStringWithNull(new Response<String>(), Response.class);
    }

    @Override
    public String upLoadRecords(Request request) {
        Response<String> response = new Response<>();
        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }

    @Override
    public String noticeOfDeviceParamsUpdate(Request request) {
        Response<String> response = new Response<>();
        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }

    @Override
    public String noticeOfCardSystemInit(Request request) {
        Response<String> response = new Response<>();
        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }

    @Override
    public String noticeOfUpgradeApp(Request request) {
        Response<String> response = new Response<>();
        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }

    @Override
    public String noticeOfResetAuthorityData(Request request) {
        Response<String> response = new Response<>();
        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }

    @Override
    public String getAccessPermission(Request request) {
        Response<String> response = new Response<>();
        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }

    @Override
    public String uploadDoorStatus(Request request) {
        Response<String> response = new Response<>();
        return JsonUtils.toJsonStringWithNull(response, Response.class);
    }
}
