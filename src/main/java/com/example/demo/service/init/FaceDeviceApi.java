package com.example.demo.service.init;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
public interface FaceDeviceApi {
    String uriPrefix = "/DevApi/";
    String getDeviceParams = uriPrefix + "GetDeviceParams";
    String setDeviceParams = uriPrefix + "SetDeviceParams";
    String getRecords = uriPrefix + "GetRecords";
    String getCharacter = uriPrefix + "GetCharacter";
    String downloadAuthorityData = uriPrefix + "DownloadAuthorityData";
    String upgradeApp = uriPrefix + "UpgradeApp";
    String restartDevice = uriPrefix + "RestartDevice";
    String getAuthorityData = uriPrefix + "GetAuthorityData";
    String setScreenSaver = uriPrefix + "SetScreenSaver";
    String remoteOpenDoor = uriPrefix + "RemoteOpenDoor";
    String getScreenSaver = uriPrefix + "GetScreenSaver";
    String setLogo = uriPrefix + "SetLogo";
    String clearDeviceData = uriPrefix + "ClearDeviceData";
    String getAllPerson = uriPrefix + "GetAllPerson";
    String deletePerson = uriPrefix + "DeletePerson";
    String collectRecords = uriPrefix + "CollectRecords";
}
