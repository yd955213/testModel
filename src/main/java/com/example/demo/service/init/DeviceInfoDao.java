package com.example.demo.service.init;

import com.alibaba.fastjson.TypeReference;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.api.UploadDoorStatus;
import com.example.demo.entity.base.DeviceInfo;
import com.example.demo.entity.base.DeviceVersion;
import com.example.demo.entity.commonInterface.RedisKeys;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author: yd
 * @date: 2022-03-03
 * @version: 1.0
 * @description:
 * @modifiedBy: 1小时刷新1次
 */
@Component
public class DeviceInfoDao {
    @Autowired
    RedisUtil redisUtil;

    /*
    下面变量中的数据 代码中经常访问，且数据量大时 查询耗时慢， 故程序启动或定时时查redis 获取数据后在jvm进行缓存，
     */
    private static final Set<String> deviceUniqueCodeSet = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> onlineDeviceSet = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> offlineDeviceSet = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> addedDownloadDeviceSet = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, DeviceInfo> DEVICE_INFO_MAP = Collections.synchronizedMap(new HashMap<>());

    public void init() {
        deviceUniqueCodeSet.clear();
        onlineDeviceSet.clear();
        offlineDeviceSet.clear();
        addedDownloadDeviceSet.clear();
        DEVICE_INFO_MAP.clear();

        getDeviceUniqueCodeSetFromRedis();
        getOnlineDeviceFromRedis();
        getOfflineDeviceFromRedis();
        getAddedDownloadDeviceSetFromRedis();
        initDeviceInfoMap();
    }

    public Map<String, DeviceInfo> initDeviceInfoMap() {
        if (ObjectUtils.isEmpty(deviceUniqueCodeSet)) getDeviceUniqueCodeSetFromRedis();

        deviceUniqueCodeSet.forEach(mac -> {
            if (!DEVICE_INFO_MAP.containsKey(mac)) {
                String deviceIp = getDeviceIpFromRedis(mac);
                String devicePort= getDevicePortFromRedis(mac);
                if(deviceIp != null && devicePort != null){
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setDeviceIp(deviceIp);
                    deviceInfo.setDevicePort(Integer.parseInt(devicePort));
                    deviceInfo.setDeviceUniqueCode(mac);
                    DEVICE_INFO_MAP.put(mac, deviceInfo);
                }
            }
        });
        return DEVICE_INFO_MAP;
    }

    public String getDeviceIp(String mac) {
        if (ObjectUtils.isEmpty(DEVICE_INFO_MAP.get(mac))) initDeviceInfoMap();
        return DEVICE_INFO_MAP.get(mac) == null ? null : DEVICE_INFO_MAP.get(mac).getDeviceIp();
    }

    public String getDevicePort(String mac) {
        return DEVICE_INFO_MAP.get(mac) == null ? null : DEVICE_INFO_MAP.get(mac).getDevicePort().toString();
    }


    private String getDeviceIpFromRedis(String mac) {
        return redisUtil.stringGet(RedisKeys.deviceInfoIpPrefix + mac);
    }

    private String getDevicePortFromRedis(String mac) {
        return redisUtil.stringGet(RedisKeys.deviceInfoPortPrefix + mac);
    }

    private void getDeviceUniqueCodeSetFromRedis() {
        Set<String> c = redisUtil.setGet(RedisKeys.deviceInfoSet);
        deviceUniqueCodeSet.addAll(c);
    }


    private void getOnlineDeviceFromRedis() {
        onlineDeviceSet.addAll(redisUtil.setGet(RedisKeys.onlineDeviceSet));
    }

    private void getOfflineDeviceFromRedis() {
        offlineDeviceSet.addAll(redisUtil.setGet(RedisKeys.offlineDeviceSet));
    }

    private void getAddedDownloadDeviceSetFromRedis() {
        addedDownloadDeviceSet.addAll(redisUtil.setGet(RedisKeys.addedDownloadDeviceSet));
    }


    public Set<String> getDeviceUniqueCodeSet() {
        if (ObjectUtils.isEmpty(deviceUniqueCodeSet) || deviceUniqueCodeSet.size() < getDeviceUniqueCodeSetSize())
            getDeviceUniqueCodeSetFromRedis();
        return deviceUniqueCodeSet;
    }

    public int getDeviceUniqueCodeSetSize() {
        return redisUtil.getSetSize(RedisKeys.deviceInfoSet);
    }

    public Set<String> getOnlineDeviceSet() {
        if (ObjectUtils.isEmpty(onlineDeviceSet) || onlineDeviceSet.size() < getOnlineDeviceSetSize())
            getOnlineDeviceFromRedis();
        return onlineDeviceSet;
    }

    public int getOnlineDeviceSetSize() {
        return redisUtil.getSetSize(RedisKeys.onlineDeviceSet);
    }

    public Set<String> getOfflineDeviceSet() {
        if (ObjectUtils.isEmpty(offlineDeviceSet) || offlineDeviceSet.size() < getOfflineDeviceSetSize())
            getOfflineDeviceFromRedis();
        return offlineDeviceSet;
    }

    public int getOfflineDeviceSetSize() {
        return redisUtil.getSetSize(RedisKeys.offlineDeviceSet);
    }

    public Set<String> getAddedDownloadDeviceSet() {
        if (ObjectUtils.isEmpty(addedDownloadDeviceSet) ||
                addedDownloadDeviceSet.size() < getAddedDownloadDeviceSetSize()) getAddedDownloadDeviceSetFromRedis();
        return addedDownloadDeviceSet;
    }

    public int getAddedDownloadDeviceSetSize() {
        return redisUtil.getSetSize(RedisKeys.addedDownloadDeviceSet);
    }

    public Map<String, DeviceInfo> getDeviceInfoMap() {
        if (ObjectUtils.isEmpty(DEVICE_INFO_MAP)) initDeviceInfoMap();
        return DEVICE_INFO_MAP;
    }

    public DeviceVersion getDeviceVersion(String mac) {
        String s = redisUtil.stringGet(RedisKeys.deviceInoUpdateVersionPredix + mac);
        return JsonUtils.parseObject(s, DeviceVersion.class);
    }

    public void saveDeviceVersion(String mac, DeviceVersion deviceVersion) {
        redisUtil.stringSet(RedisKeys.deviceInoUpdateVersionPredix + mac,
                JsonUtils.toJsonStringWithNull(deviceVersion, DeviceVersion.class));
    }

    public void saveDeviceDoorStatus(String mac, String doorStatusJson) {
        redisUtil.setAndExpireByString(RedisKeys.deviceInoDoorStatusPredix + mac, doorStatusJson, 300L);
    }

    public Request<UploadDoorStatus> getDeviceDoorStatus(String mac) {
        String s = redisUtil.stringGet(RedisKeys.deviceInoDoorStatusPredix + mac);
        return JsonUtils.parseObject(s, new TypeReference<Request<UploadDoorStatus>>() {
        }.getType());
    }

    public String getLastRebootDeviceTime(String mac){
        return redisUtil.stringGet(RedisKeys.rebootDeviceTimePredix + redisUtil.getKey(mac, getRebootDeviceCount(mac)));
    }

    public String getRebootDeviceTime(String mac, String times){
        return redisUtil.stringGet(RedisKeys.rebootDeviceTimePredix + redisUtil.getKey(mac, times));
    }

    public void setRebootDeviceTime(String mac, String TimeStamp){
        redisUtil.increment(RedisKeys.rebootDeviceCountPredix + mac);
        String rebootDeviceCount = getRebootDeviceCount(mac);
        String key = RedisKeys.rebootDeviceTimePredix + redisUtil.getKey(mac, rebootDeviceCount);
        redisUtil.stringSet(key, TimeStamp);
    }
    public String getRebootDeviceCount(String mac){
        return redisUtil.stringGet(RedisKeys.rebootDeviceCountPredix + mac);
    }


    public String getLastRestartDeviceTime(String mac){
        return redisUtil.stringGet(RedisKeys.restartDeviceTimePredix + redisUtil.getKey(mac, getRestartDeviceCount(mac)));
    }

    public String getRestartDeviceTime(String mac, String times){
        return redisUtil.stringGet(RedisKeys.restartDeviceTimePredix + redisUtil.getKey(mac, times));
    }

    public void setRestartDeviceTime(String mac, String TimeStamp){
        redisUtil.increment(RedisKeys.restartDeviceTimePredix + mac);
        String restartDeviceCount = getRestartDeviceCount(mac);
        String key = RedisKeys.restartDeviceTimePredix + redisUtil.getKey(mac, restartDeviceCount);
        redisUtil.stringSet(key, TimeStamp);
    }
    public String getRestartDeviceCount(String mac){
        return redisUtil.stringGet(RedisKeys.restartDeviceTimePredix + mac);
    }

}
