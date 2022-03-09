package com.example.demo.service.impl;

import com.example.demo.entity.api.*;
import com.example.demo.entity.base.DeviceVersion;
import com.example.demo.entity.commonInterface.RedisKeys;
import com.example.demo.service.ServerApiService;
import com.example.demo.service.init.DeviceInfoDao;
import com.example.demo.service.init.DeviceInfoManage;
import com.example.demo.service.init.FaceDeviceApiUri;
import com.example.demo.service.init.PersonInfoManage;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.redis.RedisUtil;
import com.example.demo.utils.restTemplateUtil.RestTemplateUtil;
import com.example.demo.utils.restTemplateUtil.UrlUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    RestTemplateUtil restTemplateUtil;
    @Autowired
    PersonInfoManage personInfoManage;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    DeviceInfoManage deviceInfoManage;

    @Override
    public String save(Request<DeviceHeartBeat> DeviceHeartBeatRequest) {
//        String deviceUniqueCode = DeviceHeartBeatRequest.getDeviceUniqueCode();
        log.info("心跳上报接口， 当前设备信息{}", DeviceHeartBeatRequest.getData());
        String time = MyLocalTimeUtil.getLocalDataTime();

        Response<DeviceHeartBeat> response = new Response<>();
        DeviceHeartBeat deviceHeartBeat = new DeviceHeartBeat();
        deviceHeartBeat.setServerTime(time);
        response.setTimeStamp(time);
        response.setData(deviceHeartBeat);

        String s = JsonUtils.toJsonStringNotNull(response, new TypeToken<Response<DeviceHeartBeat>>() {
        }.getType());
        log.info("心跳上报接口， 返回数据：{}", s);

//        类 RequestInterceptor 已做上线处理 这里不做任何处理
        return s;
    }

    /**
     * 根据设备mac 地址下载随机的 downloadCount（默认：30） 个 未下载的权限权限数据
     *
     * @param deviceUniqueCode 设备mac 地址
     */
    public void downloadAuthority(String deviceUniqueCode) {
        /*
         * 进行 人员信息初始化
         */

        //100人下载 设备概率 卡死 改为30人
        int downloadCount = 2;

        /*
        redis 键： personInfo:uniqueCode:set 为空，进行初始化人员操作
         */
        Set<String> personInfoUniqueCodeSet = personInfoManage.getPersonInfoUniqueCodeSet();
        System.out.println("personInfoUniqueCodeSet.size() = " + personInfoUniqueCodeSet.size());
        if (personInfoUniqueCodeSet.isEmpty()) {
            new Thread(() -> personInfoManage.init()).start();
            // 循环等待，直到 返回的set 长度 >= 需要下载的长度时，退出循环
            while (personInfoManage.getUniqueCodeSetSize() < downloadCount) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        /*
        如果设备没有 授权人员下载 进行授权
         */
        if (!redisUtil.hasValueInSet(RedisKeys.addedDownloadDeviceSet, deviceUniqueCode)) {
            deviceInfoManage.addDeviceAuthority(deviceUniqueCode);
        }

        /*
        发送权限下发请求
         */
        String deviceIp = deviceInfoManage.getDeviceIp(deviceUniqueCode);
        String devicePort = deviceInfoManage.getDevicePort(deviceUniqueCode);
        if(deviceIp != null && devicePort !=null){
            /*
             * 合成权限下载数据
             */
            Request<List<DownloadAuthorityData>> downloadAuthorityDataRequest = new Request<>();
            downloadAuthorityDataRequest.setTimeStamp(MyLocalTimeUtil.getLocalDataTime());
            downloadAuthorityDataRequest.setDeviceUniqueCode(deviceUniqueCode);
            List<DownloadAuthorityData> downloadAuthorityDataList = personInfoManage.getDownloadAuthorityDataList(deviceUniqueCode,downloadCount);
            // 无可下载人员时 退出
            if(downloadAuthorityDataList.size() == 0) return;
            downloadAuthorityDataRequest.setData(downloadAuthorityDataList);

            String requestParam = JsonUtils.toJsonStringNotNull(downloadAuthorityDataRequest,
                    new TypeToken<Request<List<DownloadAuthorityData>>>() {
                    }.getType());

            String url = UrlUtil.getUrl(deviceIp,devicePort,
                    FaceDeviceApiUri.downloadAuthorityData);
            log.info("下载人员身份数据：Url={}, 数据长度：{}", url, requestParam.length());
            restTemplateUtil.post(url, requestParam);

            // 暂时不考虑是否下载成功，将下载标志位置为false
            List<String> uniqueCodeList = new ArrayList<>();
            downloadAuthorityDataList.forEach(downloadAuthorityData -> uniqueCodeList.add(downloadAuthorityData.getUniqueCode()));
            personInfoManage.downLoading(deviceUniqueCode, uniqueCodeList);
        }

    }

    @Override
    public String noticeOfDownloadAuthorityData(Request<NoticeOfDownloadAuthorityData> noticeOfDownloadAuthorityDataRequest) {
        String isReady = noticeOfDownloadAuthorityDataRequest.getData() == null ? null : noticeOfDownloadAuthorityDataRequest.getData().getIsReady();
        String deviceUniqueCode = noticeOfDownloadAuthorityDataRequest.getDeviceUniqueCode();

        if ("Y".equals(isReady)) {
            new Thread(() -> downloadAuthority(deviceUniqueCode)).start();
        }

        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String uploadAuthorityDealResult(Request<UploadAuthorityDealResult> uploadAuthorityDealResultRequest) {
        String deviceUniqueCode = uploadAuthorityDealResultRequest.getDeviceUniqueCode();
        String code = null;
        String msg = null;
        String uniqueCode = null;
        if (uploadAuthorityDealResultRequest.getData() != null) {
            code = uploadAuthorityDealResultRequest.getData().getCode();
            msg = uploadAuthorityDealResultRequest.getData().getMsg();
            uniqueCode = uploadAuthorityDealResultRequest.getData().getUniqueCode();
        }

        personInfoManage.updateDownLoadMassageCount(deviceUniqueCode, uniqueCode);
        if ("0".equals(code)) {
            log.info("设备：{},处理权限数据成功，处理结果：{}", deviceUniqueCode, msg);
            personInfoManage.downloadSuccess(deviceUniqueCode, uniqueCode, msg);
        } else {
            log.info("设备：{},处理权限数据失败！，处理结果{}，失败信息：{}", deviceUniqueCode, code, msg);
            personInfoManage.downloadFail(deviceUniqueCode, uniqueCode, msg);
        }

        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String upLoadRecords(Request<UpLoadRecords> request) {
        // 保存记录
        deviceInfoManage.saveRecord(request.getDeviceUniqueCode(), request.getData());

        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String noticeOfDeviceParamsUpdate(Request<NoticeOfDeviceParamsUpdate> request) {
        /*
         * 暂时不做处理
         */
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String noticeOfCardSystemInit(Request<NoticeOfCardSystemInit> request) {
        /*
         * 暂时不做处理
         */
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Autowired
    DeviceInfoDao deviceInfoDao;

    @Override
    public String noticeOfUpgradeApp(Request<NoticeOfUpgradeApp> request) {
        String key = RedisKeys.deviceInoUpdateVersionPredix + request.getDeviceUniqueCode();
        if (request.getData() != null) {
            DeviceVersion deviceVersion = redisUtil.hasKey(key) ?
                    deviceInfoDao.getDeviceVersion(request.getDeviceUniqueCode()) : new DeviceVersion();
            /*
            先做保存软件信息， 硬件信息 接口未实现，先不做
             */
            deviceVersion.setDeviceUniqueCode(request.getDeviceUniqueCode());
            deviceVersion.setLastTimeSoftwareVersion(deviceVersion.getSoftwareVersionUpdateTime());
            deviceVersion.setHardwareVersionUpdateTime(request.getTimeStamp());
            deviceVersion.setSoftwareVersion(request.getData().getAppVersion());

            deviceInfoDao.saveDeviceVersion(request.getDeviceUniqueCode(), deviceVersion);
        }

        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String noticeOfResetAuthorityData(Request<String> request) {
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String getAccessPermission(Request<GetAccessPermissionRequest> request) {
        GetAccessPermissionResponse getAccessPermissionResponse = new GetAccessPermissionResponse();
        getAccessPermissionResponse.setPermission("Y");
        getAccessPermissionResponse.setDescription("区域内无进场记录");

        Response<GetAccessPermissionResponse> response = new Response<>();
        response.setData(getAccessPermissionResponse);

        String responseJson = JsonUtils.toJsonStringWithNull(response, new TypeToken<Response<GetAccessPermissionResponse>>(){}.getType());
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }

    @Override
    public String uploadDoorStatus(Request<UploadDoorStatus> request) {
        deviceInfoManage.saveDeviceDoorStatus(
                request.getDeviceUniqueCode(),
                JsonUtils.toJsonStringWithNull(request, new TypeToken<Request<UploadDoorStatus>>(){}.getType())
        );
        Response<String> response = new Response<>();
        String responseJson = JsonUtils.toJsonStringWithNull(response, Response.class);
        log.info("返回数据：{}", responseJson);
        return responseJson;
    }
}
