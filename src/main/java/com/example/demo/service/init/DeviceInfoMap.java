package com.example.demo.service.init;

import com.example.demo.entity.api.DeviceHeartBeat;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.base.DeviceInfo;
import com.example.demo.entity.base.Record;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 项目初期，未使用数据库，这里有变量来临时存储数据
 * @modifiedBy:
 */

public class DeviceInfoMap {
    private final static Map<String, Boolean> isDownloadMap = Collections.synchronizedMap(new HashMap<>());
    // 当有设备上报心跳接口时， 设备信息添加到deviceInfoMap 中， 后期使用数据库时，请冲数据库中读取；
    private static final Map<String, DeviceInfo> deviceInfoMap = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Integer> deviceRebootMap = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, String> contrastMap = Collections.synchronizedMap(new HashMap<>());
    // 用于判断设备上线、下线的集合
    private final static Set<String> offSet = Collections.synchronizedSet(new HashSet<>());
    private static Set<String> onlineSet = Collections.synchronizedSet(new HashSet<>());

    public static Map<String, Boolean> getIsDownloadMap() {
        return isDownloadMap;
    }

//    private final static Map<String, HashMap<String, Record>> recordsHashMap = Collections.synchronizedMap(new HashMap<>());
    private final static Map<String, Integer> recordsHashMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * 设备上报心跳时进行 是否下载的标识为true，权限下载之后将标识置为false，在做一个定时任务，10分钟后 将下载状态置为true
     * @param devId 设备唯一标识号
     */
    public static void initDownloadMap(String devId){
        if(!isDownloadMap.containsKey(devId))
            isDownloadMap.put(devId, true);
    }

    /**
     *  设计 只针对 部门设备进行权限下载操作
     * @param devs 需要下载的 设备列表
     */
    public static void setDownloadMapTrue(List<String> devs){
        isDownloadMap.keySet().forEach(devId -> {
            if(devs.contains(devId)) isDownloadMap.put(devId, true);
        });
    }

    public static void deviceRebootCount(String mac){
//        deviceRebootMap.put(mac, deviceRebootMap.containsKey(mac)?deviceRebootMap.get(mac) + 1: 1);
        if (deviceRebootMap.containsKey(mac)){
            deviceRebootMap.put(mac,  deviceRebootMap.get(mac) + 1);
        }else {
            deviceRebootMap.put(mac, 1);
        }
    }

    /**
     * 设备上线
     * @param deviceHeartBeat deviceHeartBeat
     */
    public static void online(Request<DeviceHeartBeat> deviceHeartBeat){
        // 存放设备信息
        String deviceUniqueCode = deviceHeartBeat.getDeviceUniqueCode();
        DeviceInfo deviceInfo = deviceInfoMap.get(deviceUniqueCode);
        deviceInfo.setCurrentTime(System.currentTimeMillis());
        deviceInfo.setTimeStamp(deviceInfo.getTimeStamp());

        // Unnecessary 'contains()' check UnsupportedOperationException: null
        if(!onlineSet.contains(deviceUniqueCode))
            onlineSet.add(deviceUniqueCode);
        // 设备上线后，删除 离线的数据
        if(offSet.contains(deviceUniqueCode))
            offSet.remove(deviceUniqueCode);
    }

    /**
     *  设备
     * @param mac mac
     */
    public static void addOfflineDev(String mac){
        offSet.add(mac);
    }


    public static void addRecordMap(String mac) {
        if(ObjectUtils.isEmpty(mac)) return;

        if(!recordsHashMap.containsKey(mac) || ObjectUtils.isEmpty(recordsHashMap.get(mac))){
            recordsHashMap.put(mac, 1);
        }else {
            recordsHashMap.put(mac, recordsHashMap.get(mac) + 1);
        }
    }

//    /**
//     * 设备对应 人 识别记录上报时 对应记录数加1
//     *
//     * @param mac        设备
//     * @param record 人
//     */
//    public static void addRecordMap(String mac, Record record) {
//        String uniqueCode = record.getUniqueCode();
//        if(ObjectUtils.isEmpty(mac) && ObjectUtils.isEmpty(uniqueCode)){
//            return;
//        }
//        if (recordsHashMap.containsKey(mac)) {
//            if (ObjectUtils.isEmpty(recordsHashMap.get(mac).get(uniqueCode))) {
//                recordsHashMap.get(mac).put(uniqueCode, record);
//            } else {
//                recordsHashMap.get(mac).get(uniqueCode).setCount(recordsHashMap.get(mac).get(uniqueCode).getCount() + 1);
//            }
//        }else {
//            HashMap<String, Record> temp = new HashMap<>();
//            temp.put(uniqueCode, record);
//            recordsHashMap.put(mac, temp);
//        }
//    }

    public static Set<String> getOffSet() {
        return offSet;
    }

    public static Map<String, DeviceInfo> getDeviceInfoMap() {
        return deviceInfoMap;
    }

    public static void setOnlineSet(Set<String> onlineSet) {
        DeviceInfoMap.onlineSet = onlineSet;
    }

    public static Set<String> getOnlineSet() {
        return onlineSet;
    }

    public static Map<String, String> getContrastMap() {
        return contrastMap;
    }

    public static Map<String, Integer> getDeviceRebootMap() {
        return deviceRebootMap;
    }
//    public static Map<String, HashMap<String, Record>> getRecordsHashMap() {
//        return recordsHashMap;
//    }
    public static Map<String, Integer> getRecordsHashMap() {
        return recordsHashMap;
    }
}
