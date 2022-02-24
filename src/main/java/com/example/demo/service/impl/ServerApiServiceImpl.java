package com.example.demo.service.impl;

import com.example.demo.entity.api.*;
import com.example.demo.entity.base.DeviceInfo;
import com.example.demo.entity.base.Record;
import com.example.demo.service.ServerApiService;
import com.example.demo.service.init.DeviceInfoMap;
import com.example.demo.service.init.FaceDeviceApiUri;
import com.example.demo.service.init.PersonInfo;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.restTemplateUtil.UrlUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import com.example.demo.utils.restTemplateUtil.HttpHeadersUtil;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    PersonInfo personInfo;

    @Override
    public String save(Request<DeviceHeartBeat> DeviceHeartBeatRequest) {
        log.info("心跳上报接口， 当前设备信息{}", DeviceInfoMap.getDeviceInfoMap().get(DeviceHeartBeatRequest.getDeviceUniqueCode()));
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

        DeviceInfoMap.online(DeviceHeartBeatRequest);
        return s;
    }

    public void downloadAuthority(String deviceUniqueCode){
        /*
         * 进行 人员信息初始化
         */

        //100人下载 设备概率 卡死 改为30人
        int downloadCount = 30;
        if(PersonInfo.getPersonInfoMap().isEmpty()){
            new Thread(()-> personInfo.init()).start();
            while (PersonInfo.getPersonInfoMap().keySet().size() <= downloadCount){
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        /*
         * 开始进行权限下载
         */
        DeviceInfo deviceInfo = DeviceInfoMap.getDeviceInfoMap().get(deviceUniqueCode);

        Request<List<DownloadAuthorityData>> downloadAuthorityDataRequest = new Request<>();
        downloadAuthorityDataRequest.setTimeStamp(MyLocalTimeUtil.getLocalDataTime());
        downloadAuthorityDataRequest.setDeviceUniqueCode(deviceUniqueCode);

        List<DownloadAuthorityData> downloadAuthorityDataList = new ArrayList<>();
        /*
        当人数超过100人时，随机下载100个；
     * 100人下载 设备概率 卡死 改为30人
         */
        List<String> uniqueCodeList = new ArrayList<>(PersonInfo.getPersonInfoMap().keySet());
        HashSet<String> uniqueCodeSet= new HashSet<>();
        List<String> nameList = new ArrayList<>();
        // 获取 随机的100个不同的人
        if(uniqueCodeList.size() > downloadCount) {
            int subscript;
            for(int i = 0; i < downloadCount; i++){
                subscript = new Random().nextInt(uniqueCodeList.size());
                while (uniqueCodeSet.contains(uniqueCodeList.get(subscript))){
                    subscript = new Random().nextInt(uniqueCodeList.size());
                }
                uniqueCodeSet.add(uniqueCodeList.get(subscript));
                nameList.add(PersonInfo.getPersonInfoMap().get(uniqueCodeList.get(subscript)).getPersonName());
            }
        }else {
            downloadCount = uniqueCodeList.size();
            uniqueCodeList.forEach(uniqueCode -> {
                uniqueCodeSet.add(uniqueCode);
                nameList.add(PersonInfo.getPersonInfoMap().get(uniqueCode).getPersonName());
            });

        }
        uniqueCodeSet.forEach(uniqueCode -> downloadAuthorityDataList.add(PersonInfo.getPersonInfoMap().get(uniqueCode)));

        downloadAuthorityDataRequest.setData(downloadAuthorityDataList);

        String requestParam = JsonUtils.toJsonStringNotNull(downloadAuthorityDataRequest,
                new TypeToken<Request<List<DownloadAuthorityData>>>() {}.getType());
//        log.info("权限下载参数：{}", requestParam);
        /*
        发送权限下发请求
         */
        HttpHeaders headers = HttpHeadersUtil.getHeaders();
        HttpEntity<String> httpEntity = new HttpEntity<>(requestParam, headers);
        String url = UrlUtil.getUrl(deviceInfo.getDeviceIp(), deviceInfo.getDevicePort(), FaceDeviceApiUri.downloadAuthorityData);

        log.info("下载人员身份数据：Url={}, 数据长度：{}", url, requestParam.length());
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            log.info("设备返回：{}", responseEntity.getBody());
        }catch (Exception e){
            log.error("下载人员身份数据：Url={}, 设备: {} 未返回数据", url, deviceUniqueCode);
        }

        // 暂时不考虑是否下载成功，将下载标志位置为false
        DeviceInfoMap.getIsDownloadMap().put(deviceUniqueCode, false);
        personInfo.addDownload(deviceUniqueCode, uniqueCodeSet.size(), uniqueCodeSet);
    }

    @Override
    public String noticeOfDownloadAuthorityData(Request<NoticeOfDownloadAuthorityData> noticeOfDownloadAuthorityDataRequest) {
        String isReady = noticeOfDownloadAuthorityDataRequest.getData().getIsReady();
        String deviceUniqueCode = noticeOfDownloadAuthorityDataRequest.getDeviceUniqueCode();

        if("Y".equals(isReady)){
            if (DeviceInfoMap.getIsDownloadMap().containsKey(deviceUniqueCode) &&
                    DeviceInfoMap.getIsDownloadMap().get(deviceUniqueCode)){
                new Thread(()->downloadAuthority(deviceUniqueCode)).start();
            }
        }

        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String uploadAuthorityDealResult(Request<UploadAuthorityDealResult> uploadAuthorityDealResultRequest) {
        String deviceUniqueCode = uploadAuthorityDealResultRequest.getDeviceUniqueCode();
        String code = uploadAuthorityDealResultRequest.getData().getCode();
        String msg = uploadAuthorityDealResultRequest.getData().getMsg();
        String uniqueCode = uploadAuthorityDealResultRequest.getData().getUniqueCode();
        if("0".equals(code)){
            log.info("设备：{},处理权限数据成功，处理结果：{}", deviceUniqueCode, msg);
            personInfo.downloadSuccess(deviceUniqueCode, uniqueCode);
        }else {
            log.info("设备：{},处理权限数据失败！，处理结果{}，失败信息：{}", deviceUniqueCode, code, msg);
            Map<String, HashMap<String,  PersonInfo.ErrorPerson>> downErrorInfoMap = PersonInfo.getDownErrorInfoMap();
            //
            if(downErrorInfoMap.containsKey(deviceUniqueCode) &&
                    !ObjectUtils.isEmpty(downErrorInfoMap.get(deviceUniqueCode)) &&
                    downErrorInfoMap.get(deviceUniqueCode).containsKey(uniqueCode)){
                downErrorInfoMap.get(deviceUniqueCode).get(uniqueCode).setErrorMsg(msg);
            }
//            else {
//                HashMap<String, PersonInfo.ErrorPerson> temp = new HashMap<>();
//                downErrorInfoMap.put(deviceUniqueCode, temp);
//            }
        }
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String upLoadRecords(Request<UpLoadRecords> request) {
        // 保存记录
//        Record record = new Record();
//        record.setCount(1);
//        record.setUniqueCode(request.getData().getUniqueCode());
//        record.setName(PersonInfo.getPersonInfoMap().get(request.getData().getUniqueCode()).getPersonName());
//        DeviceInfoMap.addRecordMap(request.getDeviceUniqueCode(), record);
        DeviceInfoMap.addRecordMap(request.getDeviceUniqueCode());
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String noticeOfDeviceParamsUpdate(Request request) {
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String noticeOfCardSystemInit(Request request) {
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String noticeOfUpgradeApp(Request request) {
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String noticeOfResetAuthorityData(Request request) {
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String getAccessPermission(Request request) {
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String uploadDoorStatus(Request request) {
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }
}
