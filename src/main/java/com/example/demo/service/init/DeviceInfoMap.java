package com.example.demo.service.init;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 项目初期，未使用数据库，这里有变量来临时存储数据
 * @modifiedBy:
 */
public class DeviceInfoMap {
    private final static Map<String, Boolean> isDownloadMap = Collections.synchronizedMap(new HashMap<>());

    public static Map<String, Boolean> getIsDownloadMap() {
        return isDownloadMap;
    }

    /**
     * 设备上报心跳时进行 是否下载的标识，权限下载之后将标识置为false，在做一个定时任务，10分钟后 将下载状态置为true
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
}
