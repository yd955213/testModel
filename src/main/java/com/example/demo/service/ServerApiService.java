package com.example.demo.service;

import com.example.demo.entity.api.*;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
public interface ServerApiService {
    String save(Request<DeviceHeartBeat> devicesHeartBeatServiceRequest);
    String noticeOfDownloadAuthorityData(Request<NoticeOfDownloadAuthorityData> noticeOfDownloadAuthorityDataRequest);
    String uploadAuthorityDealResult(Request<UploadAuthorityDealResult> uploadAuthorityDealResultRequest);
    String upLoadRecords(Request<UpLoadRecords> request);
    String noticeOfDeviceParamsUpdate(Request<NoticeOfDeviceParamsUpdate> request);
    String noticeOfCardSystemInit(Request<NoticeOfCardSystemInit> request);
    String noticeOfUpgradeApp(Request<NoticeOfUpgradeApp> request);
    String noticeOfResetAuthorityData(Request<String> request);

    String getAccessPermission(Request<GetAccessPermissionRequest> request);

    String uploadDoorStatus(Request<UploadDoorStatus> request);
}
