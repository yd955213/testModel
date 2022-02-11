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
    String upLoadRecords(Request request);
    String noticeOfDeviceParamsUpdate(Request request);
    String noticeOfCardSystemInit(Request request);
    String noticeOfUpgradeApp(Request request);
    String noticeOfResetAuthorityData(Request request);

    String getAccessPermission(Request request);

    String uploadDoorStatus(Request request);
}
