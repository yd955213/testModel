package com.example.demo.conctroller;

import com.example.demo.entity.api.*;
import com.example.demo.service.impl.ServerApiServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description: 处理人脸设备请求
 * @modifiedBy:
 */
@RestController
@RequestMapping("/ServerApi")
@Log4j2
public class ServerApiController {
    @Autowired
    ServerApiServiceImpl devicesHeartBeatService;

    /**
     * 3.1 上报心跳
     * 设备主动定时循环请求
     */
    @PostMapping("/DeviceHeartBeat")
    public String deviceHeartBeat(@RequestBody Request<DeviceHeartBeat> devicesHeartBeatRequest){
        return devicesHeartBeatService.save(devicesHeartBeatRequest);
    }

    /**
     * 3.2上报记录
     * 设备完成身份识别及业务动作后，数据立即上报
     * 离线时数据缓存在设备内，待网络恢复后再上报
     */
    @PostMapping("/UploadRecords")
    public String upLoadRecords(@RequestBody Request<UpLoadRecords> upLoadRecordsRequest){
        return devicesHeartBeatService.upLoadRecords(upLoadRecordsRequest);
    }

    /**
     * 3.3上报身份数据处理结果
     * 设备接收到服务器下发的身份数据，每处理完一条权限后主动请求
     */
    @PostMapping("/UploadAuthorityDealResult")
    public String uploadAuthorityDealResult(@RequestBody Request<UploadAuthorityDealResult> uploadAuthorityDealResultRequest){
        return devicesHeartBeatService.uploadAuthorityDealResult(uploadAuthorityDealResultRequest);
    }


    /**
     * 3.4设备参数更新通知
     * 暂时不做处理
     */
    @PostMapping("/NoticeOfDeviceParamsUpdate")
    public String noticeOfDeviceParamsUpdate(@RequestBody Request<NoticeOfDeviceParamsUpdate> request){
        return devicesHeartBeatService.noticeOfDeviceParamsUpdate(request);
    }

    /**
     * 3.5设备子卡初始化通知
     * 设备完成子卡初始化操作后触发
     * 暂时不做处理
     */
    @PostMapping("/NoticeOfCardSystemInit")
    public String noticeOfCardSystemInit(@RequestBody Request<NoticeOfCardSystemInit> request){
        return devicesHeartBeatService.noticeOfCardSystemInit(request);
    }

    /**
     * 3.6下发身份数据通知
     */
    @PostMapping("/NoticeOfDownloadAuthorityData")
    public String noticeOfDownloadAuthorityData(@RequestBody Request<NoticeOfDownloadAuthorityData> noticeOfDownloadAuthorityDataRequest){
        return devicesHeartBeatService.noticeOfDownloadAuthorityData(noticeOfDownloadAuthorityDataRequest);
    }


    /**
     * 3.7设备升级完成通知
     */
    @PostMapping("/NoticeOfUpgradeApp")
    public String noticeOfUpgradeApp(@RequestBody Request<NoticeOfUpgradeApp> request){

        return devicesHeartBeatService.noticeOfUpgradeApp(request);
    }
    /**
     * 3.8重置权限数据通知
     */
    @PostMapping("/NoticeOfResetAuthorityData")
    public String noticeOfResetAuthorityData(@RequestBody Request<String> request){
        return devicesHeartBeatService.noticeOfResetAuthorityData(request);
    }

    /**
     *3.9获取人员通行权限
     * 设备开启防潜回功能时，识别人员后是否允许通行，由服务端根据设备的进场、出场、区域属性以及人员的历史进出记录等条件来判断。禁止通行的场景包括但不限于：已进未出时企图再次进场，已出未进时企图再次出场等
     */
    @PostMapping("/GetAccessPermission")
    public String getAccessPermission(@RequestBody Request<GetAccessPermissionRequest> request){
        return devicesHeartBeatService.getAccessPermission(request);
    }

    /**
     * 3.10上报设备门禁状态
     * 暂时不做处理
     */
    @PostMapping("/UploadDoorStatus")
    public String uploadDoorStatus(@RequestBody Request<UploadDoorStatus> request){
        return devicesHeartBeatService.uploadDoorStatus(request);
    }
}
