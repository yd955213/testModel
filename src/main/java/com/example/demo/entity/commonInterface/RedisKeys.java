package com.example.demo.entity.commonInterface;

/**
 * @author: yd
 * @date: 2022-02-24
 * @version: 1.0
 * @description: redis 对应的 键值， 注释中{} 内的内容实际使用时需要替换
 * @modifiedBy:
 */
public interface RedisKeys {
    /**
     *
     存储权限下载的人员信息：redis 的key personInfo:uniqueCode:{uniqueCode} value 为 DownloadAuthorityData 的bean ： json 格式字符串
     */
    String personInfoUniqueCodePrefix = "personInfo:uniqueCode:";
    /**
     * 存储 所有的 uniqueCode 编号
     */
    String personInfoUniqueCodeSet = "personInfo:uniqueCode:set";
    /**
     存储 人员的图片路径 redis 的key： personInfo:picture:uri:{uniqueCode}";
     */
    String personPictureUri = "personInfo:picture:uri:";

    /**
     * 存储 已授权人员下载的 设备集合
     */
    String addedDownloadDeviceSet = "deviceInfo:addedDownload:set";
    /**
     * 存储 该人员信息是否已下载 redis 的key personInfo:isDown:{设备mac}:{uniqueCode} value 为 0：未下载 1：已下载
     * 注意：使用需要调用 redisUtil.getKey() 合成两个 {} 中的内容
     */
    String isDownPrefix = "personInfo:isDown:";
    /**
     * 存储 每个设备权限 已下载 uniqueCode 集合 redis 的key personInfo:downloaded:uniqueCode:set:{设备mac}:
     */
    String downloadedUniqueCodeSet = "personInfo:downloaded:uniqueCode:set:";
    /**
     * 存储 每个设备权限未下载 uniqueCode 集合 redis 的key personInfo:not:downloaded:uniqueCode:set:{设备mac}:
     */
    String notDownloadUniqueCodeSet = "personInfo:not:downloaded:device:uniqueCodeSet:";
    /**
     * 存储 所有设备权限下载成功的 uniqueCode 集合
     */
    String downloadSuccessUniqueCodeSet = "personInfo:download:success:uniqueCodeSet";
    /**
     存储 每台设备权限下载成功的 uniqueCode 集合 redis 的key：personInfo:download:success:uniqueCode:Set:{mac}
     */
    String downloadSuccessSetByMacPrefix = "personInfo:download:success:uniqueCode:Set:";
    /**
     * 存储 每台设备权限下载失败的 uniqueCode 集合  redis 的key： personInfo:download:fail:uniqueCode:Set:{mac}
     */
    String downloadFailSetByMacPrefix = "personInfo:download:fail:uniqueCode:Set:";
    /**
     * 存储 所有设备权限下载失败的 uniqueCode 集合
     */
    String downloadFailUniqueCodeSet = "personInfo:download:fail:uniqueCodeSet";
    /**
     * 存储 每台设备权限下载失败的信息 redis 的key personInfo:download:fail:message:{设备mac}:{uniqueCode}
     * 注意：使用需要调用 redisUtil.getKey() 合成两个 {} 中的内容
     */
    String downloadMessagePrefix = "personInfo:download:message:";
    /**
     * 存储 每台设备权限下载失败的数量 redis 的key personInfo:download:message:count:{massage}
     */
    String downloadMessageCountPrefix = "personInfo:download:message:count:";
    String downloadMessageSet = "personInfo:download:message:set";

    /**
     * 存储 所有的 设备mac 集合
     */
    String deviceInfoSet = "deviceInfo:deviceMac:set";
    /**
     * 存储 每台设备ip信息 redis 的key：  deviceInfo:ip:{mac}
     */
    String deviceInfoIpPrefix = "deviceInfo:ip:";
    /**
     * 存储 每台设备port信息 redis 的key：  deviceInfo:port:{mac}
     */
    String deviceInfoPortPrefix = "deviceInfo:port:";
    /**
     * 存储 每台设备最后一次上线的时间戳信息 redis 的key：  deviceInfo:timestamp:{mac}
     */
    String deviceInfoTimestampPrefix = "deviceInfo:timestamp:";

    /**
     * 存储 设备线存储mac, 1秒中过期， redis 的key：  deviceInfo:online:tamp:{mac}
     */
    String onlineDeviceTemp = "deviceInfo:online:tamp:";
    String onlineDeviceSet = "deviceInfo:mac:online:set";
    String offlineDeviceSet = "deviceInfo:mac:offline:set";

    /**
     * 存储 通讯记录信息 redis 的key：recordInfo:{mac}:{记录号}
     * 注意：使用需要调用 redisUtil.getKey() 合成两个 {} 中的内容
     */
    String recordInfoPredix = "recordInfo:";
    /**
     * 存储 每台共多少条记录数 redis 的key： record:count:{mac}
     */
    String recordCountPredix = "record:count:";

    /**
     * 存储 设备更新版本 的信息  redis 的key： deviceInfo:updateVersion:{mac}  值：类 DeviceVersion 的 bean 的jsonString
     */
    String deviceInoUpdateVersionPredix = "deviceInfo:updateVersion:";

    /**
     * 存储 设备的
     * 锁的状态     0：关闭 1：开门
     * 按键状态     0：未按下 1：按键开门
     * 门磁状态     0：关闭 1：开门
     * 消防报警状态   0：未报警 1：报警
     * 等信息  redis 的key： deviceInfo:updateVersion:{mac}  值：类 DeviceVersion 的 bean 的jsonString
     */
    String deviceInoDoorStatusPredix = "deviceInfo:door:status:";
    /**
     * 存储 向设备发送主动重启命令的次数  redis 的key：device:reboot:mac:count:{mac}
     */
    String rebootDeviceCountPredix = "device:reboot:mac:count:";
    /**
     * 存储 向设备发送主动重启命令的时间  redis 的key： device:reboot:mac:time:{mac}:{第几次重启记录}
     */
    String rebootDeviceTimePredix = "device:reboot:mac:time:";

    /**
     * 存储 设备重启次数  redis 的key：device:restart:mac:count:{mac}
     */
    String restartDeviceCountPredix = "device:restart:mac:count：";
    /**
     * 存储 设备重启时间  redis 的key： device:restart:mac:time:{mac}:{第几次重启记录}
     */
    String restartDeviceTimePredix = "device:restart:mac:time:";
}
