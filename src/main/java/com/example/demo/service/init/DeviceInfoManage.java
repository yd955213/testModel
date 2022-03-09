package com.example.demo.service.init;

import com.example.demo.entity.api.DownloadAuthorityData;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.api.UpLoadRecords;
import com.example.demo.entity.api.UploadDoorStatus;
import com.example.demo.entity.base.*;
import com.example.demo.entity.commonInterface.RedisKeys;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.redis.RedisUtil;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 项目初期，未使用数据库，这里有变量来临时存储数据
 * @modifiedBy:
 */

@Component
public class DeviceInfoManage {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    DeviceInfoDao deviceInfoDao;
    @Autowired
    PersonInfoDao personInfoDao;

    /**
     * 设备上线
     * @param deviceInfo deviceInfo
     */
    public void online(DeviceInfo deviceInfo) {
        saveDeviceInfo(deviceInfo);
        addDeviceAuthority(deviceInfo.getDeviceUniqueCode());
    }

    /**
     * redis/ deviceInfoDao.DEVICE_INFO_MAP 更新 设备信息，
     * @param deviceInfo deviceInfo
     */
    public void saveDeviceInfo(DeviceInfo deviceInfo){
        String deviceUniqueCode = deviceInfo.getDeviceUniqueCode();
        String deviceIp = deviceInfo.getDeviceIp();
        Integer devicePort = deviceInfo.getDevicePort();

        addOnlineDev(deviceUniqueCode);

        // 存放设备信息
        redisUtil.setAndExpireByString(RedisKeys.onlineDeviceTemp + deviceUniqueCode, "on", 100L);

        if(deviceUniqueCode != null && (deviceIp != null || devicePort != -1)){
            // 如果Ip 和端口变话 则修改
            if (!getDeviceUniqueCodeSet().contains(deviceUniqueCode) ||
                    !Objects.equals(deviceIp, getDeviceIp(deviceUniqueCode)) ||
                    !devicePort.equals(Integer.parseInt(getDevicePort(deviceUniqueCode)))){
                Map<String, DeviceInfo> deviceInfoMap = deviceInfoDao.getDeviceInfoMap();

                redisUtil.setAdd(RedisKeys.deviceInfoSet, deviceUniqueCode);

                if(!deviceInfoMap.containsKey(deviceUniqueCode)){
                    deviceInfoMap.put(deviceUniqueCode, deviceInfo);
                }

                if(!ObjectUtils.isEmpty(deviceIp)){
                    redisUtil.stringSet(RedisKeys.deviceInfoIpPrefix + deviceUniqueCode, deviceIp);
                    deviceInfoMap.get(deviceUniqueCode).setDeviceIp(deviceIp);
                }
                if(!ObjectUtils.isEmpty(devicePort)){
                    redisUtil.stringSet(RedisKeys.deviceInfoPortPrefix + deviceUniqueCode, devicePort.toString());
                    deviceInfoMap.get(deviceUniqueCode).setDevicePort(devicePort);
                }
                if(!ObjectUtils.isEmpty(deviceInfo.getTimeStamp())){
                    redisUtil.stringSet(RedisKeys.deviceInfoTimestampPrefix + deviceUniqueCode, deviceInfo.getTimeStamp());
                    deviceInfoMap.get(deviceUniqueCode).setTimeStamp(deviceInfo.getTimeStamp());
                }
            }
        }

    }

    public String getDeviceIp(String mac){
        return deviceInfoDao.getDeviceIp(mac);
    }

    public String getDevicePort(String mac){
        return deviceInfoDao.getDevicePort(mac);
    }

    /**
     * 获取所以设备的mac 地址
     * @return Set<String>
     */
    public Set<String> getDeviceUniqueCodeSet(){
        return deviceInfoDao.getDeviceUniqueCodeSet();
    }

    /**
     * 设备上报心跳时进行 是否下载的标识为true，权限下载之后将标识置为false，在做一个定时任务，10分钟后 将下载状态置为true
     *
     * @param devId 设备唯一标识号
     */
    public void initDownloadMap(String devId) {
    }

    /**
     * 设计 只针对 部门设备进行权限下载操作
     *
     * @param devs 需要下载的 设备列表
     */
    public void setDownloadMapTrue(List<String> devs) {
    }

    /**
     * 获取设备重启次数
     * @param mac 设备mac
     */
    private RebootMassage doGetDeviceRebootCount(String mac) {

        String rebootCount = deviceInfoDao.getRebootDeviceCount(mac);
        RebootMassage rebootMassage = new RebootMassage();
        rebootMassage.setDeviceUniqueCode(mac);
        if(ObjectUtils.isEmpty(rebootCount)) return rebootMassage;

        List<String> rebootTimeList = new ArrayList<>();
        for (int i =0; i<Integer.parseInt(rebootCount); i++){
//                rebootTimeList.add();
        }

        rebootMassage.setDeviceUniqueCode(mac);
        rebootMassage.setDeviceUniqueCode(rebootCount);
        rebootMassage.setRebootTimeList(rebootTimeList);
        return rebootMassage;
    }
    /**
     * 获取单个设备重启次数
     * @param mac 设备mac
     */
    public String getDeviceRebootCount(String mac) {
        RebootMassage rebootMassage = doGetDeviceRebootCount(mac);

        return JsonUtils.toJsonStringWithNull(rebootMassage, RebootMassage.class);
    }
    /**
     * 获取所有设备重启次数
     */
    public String getALLDeviceRebootCount() {
        List<RebootMassage> list = new ArrayList<>();
        getDeviceUniqueCodeSet().forEach(mac -> list.add(doGetDeviceRebootCount(mac)));
        return JsonUtils.toJsonStringWithNull(list, new TypeToken<List<RebootMassage>>(){}.getType());
    }


    /**
     *  获取在线设备
     * @return  Set<String>
     */
    public Set<String> getOnlineDevice() {
        checkedTheDeviceIsOnline();
        return deviceInfoDao.getOnlineDeviceSet();
    }

    /**
     * 获取离线设备
     * @return Set<String>
     */
    public Set<String> getOfflineDevice() {
        checkedTheDeviceIsOnline();
        return deviceInfoDao.getOfflineDeviceSet();
    }

    public void checkedTheDeviceIsOnline(){
        Set<String> set = deviceInfoDao.getDeviceUniqueCodeSet();
        set.forEach(mac -> {
            if (!redisUtil.hasKey(RedisKeys.onlineDeviceTemp + mac)) {
                addOfflineDev(mac);
            }
        });
    }

    /**
     * 设备离线 清除 缓存设备
     * @param deviceUniqueCode deviceUniqueCode
     */
    public void addOfflineDev(String deviceUniqueCode) {
        redisUtil.setRemove(RedisKeys.onlineDeviceSet, deviceUniqueCode);
        deviceInfoDao.getOnlineDeviceSet().remove(deviceUniqueCode);

        redisUtil.setAdd(RedisKeys.offlineDeviceSet, deviceUniqueCode);
        deviceInfoDao.getOfflineDeviceSet().add(deviceUniqueCode);

    }
    /**
     * 设备上线线 添加 缓存设备
     * @param deviceUniqueCode deviceUniqueCode
     */
    public void addOnlineDev(String deviceUniqueCode) {
        redisUtil.setAdd(RedisKeys.onlineDeviceSet, deviceUniqueCode);
        deviceInfoDao.getOnlineDeviceSet().add(deviceUniqueCode);

        redisUtil.setRemove(RedisKeys.offlineDeviceSet, deviceUniqueCode);
        deviceInfoDao.getOfflineDeviceSet().remove(deviceUniqueCode);

    }

    /**
     * 第一次上线的设备 ，根据 RedisKeys.personInfoUniqueCodeSet 中的人数，新增响应的权限 对应键值：RedisKeys.isDownPrefix；
     * 新增人员时，同理也要新增权限：对应方法：暂时未写；
     *
     * @param deviceUniqueCode 设备mac地址
     */
    public void addDeviceAuthority(String deviceUniqueCode) {
        Set<String> addedDownloadDeviceSet = deviceInfoDao.getAddedDownloadDeviceSet();
        if (!addedDownloadDeviceSet.contains(deviceUniqueCode)) {
            Set<String> uniqueCodeSet = personInfoDao.getPersonInfoUniqueCodeSet();
            if (uniqueCodeSet != null) {
                uniqueCodeSet.forEach(uniqueCode -> {
                    personInfoDao.addDownloadResult(deviceUniqueCode, uniqueCode, "0");
                    personInfoDao.addNotDownloadedUniqueCodeSet(deviceUniqueCode, uniqueCode);
                });
            }
            redisUtil.setAdd(RedisKeys.addedDownloadDeviceSet, deviceUniqueCode);
        }
    }

    /**
     *  保存 通行记录到redis 中
     * @param mac mac
     * @param records UpLoadRecords 的bean
     */
    public void saveRecord(String mac, UpLoadRecords records) {
        String recordCountKey = RedisKeys.recordCountPredix + mac;
        redisUtil.increment(recordCountKey);
        String jsonStr = JsonUtils.toJsonStringWithNull(records, UpLoadRecords.class);
        String recordCount = redisUtil.stringGet(recordCountKey);
        redisUtil.stringSet(RedisKeys.recordInfoPredix + redisUtil.getKey(mac, recordCount), jsonStr);
    }

    public String getAllRecordCount() {
        Set<String> set = deviceInfoDao.getDeviceUniqueCodeSet();
        List<RecordDevice> list = new ArrayList<>();
        set.forEach(mac->{
            RecordDevice recordDevice = new RecordDevice();
            recordDevice.setDeviceUniqueCode(mac);
            String recordCountString = redisUtil.stringGet(RedisKeys.recordCountPredix + mac);
            recordCountString = ObjectUtils.isEmpty(recordCountString)? "0": recordCountString;
            recordDevice.setRecordCount(Integer.parseInt(recordCountString));
            list.add(recordDevice);
        });
        return JsonUtils.toJsonStringNotNull(list, new TypeToken<List<RecordDevice>>(){}.getType());
    }


    /**
     * 进行权限对比操作
     * @param uniqueCodeList  uniqueCodeList
     * @return String
     */
    public String authorityContrast(String mac, List<String> uniqueCodeList){
        return JsonUtils.toJsonStringNotNull(doAuthorityContrast(mac, uniqueCodeList), DownloadMassage.class);
    }

    public DownloadMassage doAuthorityContrast(String mac, List<String> uniqueCodeList){
        DownloadMassage downloadMassage = new DownloadMassage();
        downloadMassage.setDeviceUniqueCode(mac);

        if(ObjectUtils.isEmpty(uniqueCodeList)) return downloadMassage;

        Set<String> downloadFailUniqueCodeSet = personInfoDao.getDownloadFailUniqueCodeSet();
        Set<String> downloadSuccessUniqueCodeSet = personInfoDao.getDownloadSuccessUniqueCodeSet();
        uniqueCodeList.forEach( uniqueCode ->{
            DownloadMassage.Massage massage = new DownloadMassage.Massage();
            massage.setUniqueCode(uniqueCode);
            DownloadAuthorityData downloadAuthorityData = personInfoDao.getDownloadAuthorityData(uniqueCode);

            if(null == downloadAuthorityData) {
                massage.setMassage("库中查无此人，设备有权");
            }else if(!downloadSuccessUniqueCodeSet.contains(uniqueCode)){
                massage.setName(downloadAuthorityData.PersonName);
                massage.setMassage("库中有权，设备无权");
            }else if(downloadFailUniqueCodeSet.contains(uniqueCode)){
                massage.setName(downloadAuthorityData.PersonName);
                massage.setMassage("库中记录为下载失败，设备有权");
            }
            if(!ObjectUtils.isEmpty(massage.getMassage())){
                downloadMassage.getMassageList().add(massage);
            }
        });
        return downloadMassage;
    }

    /**
     * jsonString　格式
     * {
     * "DeviceUniqueCode":"85AD0E",
     * "TimeStamp":"2020-04-0709:48:03",
     * "Data":{
     * "Lock":0,
     * "Button":0,
     * "GateMagnetism":0,
     * "Alarm":0
     * }
     * }
     * @param mac  mac
     * @param doorStatusJson jsonString
     */
    public void saveDeviceDoorStatus(String mac, String doorStatusJson){
        deviceInfoDao.saveDeviceDoorStatus(mac, doorStatusJson);
    }

    public Request<UploadDoorStatus> getDeviceDoorStatus(String mac){
        return deviceInfoDao.getDeviceDoorStatus(mac);
    }

    public void clearDownloadedAuthorityAll(){
        getDeviceUniqueCodeSet().forEach(mac -> clearDownloadedAuthority(mac));
    }
    public void clearDownloadedAuthority(String mac){
        redisUtil.deleteRedisKey(RedisKeys.downloadedUniqueCodeSet+ mac);
        redisUtil.deleteRedisKey(RedisKeys.downloadFailUniqueCodeSet+ mac);
        redisUtil.deleteRedisKey(RedisKeys.notDownloadUniqueCodeSet + mac);
        redisUtil.setRemove(RedisKeys.addedDownloadDeviceSet, mac);
    }


    public String getLastRebootDeviceTime(String mac){
        return deviceInfoDao.getLastRebootDeviceTime(mac);
    }

    public String getRebootDeviceTime(String mac, String times){
        return deviceInfoDao.getRebootDeviceTime(mac, times);
    }

    public void setRebootDeviceTime(String mac, String TimeStamp){
        deviceInfoDao.setRebootDeviceTime(mac, TimeStamp);
    }
    public String getRebootDeviceCount(String mac){
        return deviceInfoDao.getRebootDeviceCount(mac);
    }


    public String getLastRestartDeviceTime(String mac){
        return deviceInfoDao.getLastRestartDeviceTime(mac);
    }

    public String getRestartDeviceTime(String mac, String times){
        return deviceInfoDao.getRestartDeviceTime(mac, times);
    }

    public void setRestartDeviceTime(String mac, String TimeStamp){
        deviceInfoDao.setRestartDeviceTime(mac, TimeStamp);
    }
    public String getRestartDeviceCount(String mac){
        return deviceInfoDao.getRestartDeviceCount(mac);
    }
}
