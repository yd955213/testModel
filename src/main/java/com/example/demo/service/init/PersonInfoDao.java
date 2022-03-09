package com.example.demo.service.init;

import com.example.demo.entity.api.DownloadAuthorityData;
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
 * @description: 存储 redis 中 大set 的数据，防止频繁查询导致 redis 性能下降
 *    集合更新： 定时任务 1小时刷新1次   这个想法貌似有问题，无人解答，（主要是我现在的环境，redis和开发环境都部署在本机，程序跑起来后
 *    CPU 使用率 10分钟后100%：这种想法是避免 当redis 数据没有变化时，不用频繁查询）
 * @modifiedBy:
 */
@Component
public class PersonInfoDao {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    DeviceInfoDao deviceInfoDao;
    /*
    下面变量中的数据 代码中经常访问，且数据量大时 查询耗时慢， 故程序启动或定时时查redis 获取数据后在jvm进行缓存，
     */
    private static final Set<String> personInfoUniqueCodeSet = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> downloadFailUniqueCodeSet = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> downloadSuccessUniqueCodeSet = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> downloadedUniqueCodeSet = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String ,Set<String>> notDownloadedUniqueCodeMap = Collections.synchronizedMap(new HashMap<>());


    public void init(){
        personInfoUniqueCodeSet.clear();
        downloadFailUniqueCodeSet.clear();
        downloadSuccessUniqueCodeSet.clear();
        downloadedUniqueCodeSet.clear();
        notDownloadedUniqueCodeMap.clear();

        getPersonInfoUniqueCodeSet();
        getDownloadFailUniqueCodeSet();
        getDownloadSuccessUniqueCodeSet();
        getDownloadedUniqueCodeSet();
        getNotDownloadedUniqueCodeMapFromRedis();
    }

    private void getPersonInfoUniqueCodeSetFromRedis() {
        personInfoUniqueCodeSet.clear();
        personInfoUniqueCodeSet.addAll(redisUtil.setGet(RedisKeys.personInfoUniqueCodeSet));
    }
    private void getDownloadFailUniqueCodeSetFromRedis() {
        downloadFailUniqueCodeSet.clear();
        downloadFailUniqueCodeSet.addAll(redisUtil.setGet(RedisKeys.downloadFailUniqueCodeSet));
    }

    private void getDownloadSuccessUniqueSetFromRedis() {
        downloadSuccessUniqueCodeSet.clear();
        downloadSuccessUniqueCodeSet.addAll(redisUtil.setGet(RedisKeys.downloadSuccessUniqueCodeSet));
    }

    private void getDownloadedUniqueCodeSetFromRedis(){
        downloadedUniqueCodeSet.clear();
        downloadedUniqueCodeSet.addAll(redisUtil.setGet(RedisKeys.downloadedUniqueCodeSet));
    }

    private void getNotDownloadedUniqueCodeMapFromRedis(){
        deviceInfoDao.getDeviceUniqueCodeSet().forEach(mac ->
            notDownloadedUniqueCodeMap.put(mac, redisUtil.setGet(RedisKeys.notDownloadUniqueCodeSet + mac)));
    }


    public Set<String> getPersonInfoUniqueCodeSet() {
        if(ObjectUtils.isEmpty(personInfoUniqueCodeSet) ||
                personInfoUniqueCodeSet.size() != redisUtil.getSetSize(RedisKeys.personInfoUniqueCodeSet)) {
            getPersonInfoUniqueCodeSetFromRedis();
        }
        return personInfoUniqueCodeSet;
    }

    public Set<String> getDownloadFailUniqueCodeSet() {
        if(ObjectUtils.isEmpty(downloadFailUniqueCodeSet) ||
                downloadFailUniqueCodeSet.size() != redisUtil.getSetSize(RedisKeys.downloadFailUniqueCodeSet)) {
            getDownloadFailUniqueCodeSetFromRedis();
        }
        return downloadFailUniqueCodeSet;
    }

    public Set<String> getDownloadSuccessUniqueCodeSet() {
        if(ObjectUtils.isEmpty(downloadSuccessUniqueCodeSet) ||
                downloadSuccessUniqueCodeSet.size() != redisUtil.getSetSize(RedisKeys.downloadSuccessUniqueCodeSet)) {
            getDownloadSuccessUniqueSetFromRedis();
        }
        return downloadSuccessUniqueCodeSet;
    }

    public Set<String> getDownloadedUniqueCodeSet() {
        if(ObjectUtils.isEmpty(downloadedUniqueCodeSet) ||
                downloadedUniqueCodeSet.size() != redisUtil.getSetSize(RedisKeys.downloadedUniqueCodeSet)) {
            getDownloadedUniqueCodeSetFromRedis();
        }
        return downloadedUniqueCodeSet;
    }

    public void refreshPersonUniqueCodeSet(){
        int setSize = redisUtil.getSetSize(RedisKeys.personInfoUniqueCodeSet);
        if(personInfoUniqueCodeSet.size() < setSize){
            getPersonInfoUniqueCodeSetFromRedis();
        }
    }

    public Map<String, Set<String>> getNotDownloadedUniqueCodeMap() {
        Set<String> deviceUniqueCodeSet = deviceInfoDao.getDeviceUniqueCodeSet();
        boolean refresh = false;
        for (String mac: deviceUniqueCodeSet){
            if (null == notDownloadedUniqueCodeMap.get(mac)){
                refresh = true;
                break;
            }
            int setSize = getNotDownloadedUniqueCodeSetSize(RedisKeys.notDownloadUniqueCodeSet + mac);
            if(notDownloadedUniqueCodeMap.get(mac).size() < setSize){
                refresh = true;
                break;
            }
        }
        if(refresh) getNotDownloadedUniqueCodeMapFromRedis();
        return notDownloadedUniqueCodeMap;
    }

    /**
     * 添加 设备可下载的 人员
     * @param mac 设备
     * @param uniqueCode 人员uniqueCode
     */
    public synchronized void addNotDownloadedUniqueCodeSet(String mac, String uniqueCode){
        redisUtil.setAdd(RedisKeys.notDownloadUniqueCodeSet + mac, uniqueCode);
        notDownloadedUniqueCodeMap.get(mac).add(uniqueCode);
    }
    public int getNotDownloadedUniqueCodeSetSize(String mac){
        return redisUtil.getSetSize(RedisKeys.notDownloadUniqueCodeSet + mac);
    }
    /**
     * 下载后，有结果返回， 删除未下载人员的uniqueCode
     * @param mac 设备
     * @param uniqueCode 人员uniqueCode
     */
    public synchronized void removeNotDownloadedUniqueCodeSet(String mac, String uniqueCode){
        redisUtil.setRemove(RedisKeys.notDownloadUniqueCodeSet + mac, uniqueCode);
        notDownloadedUniqueCodeMap.get(mac).remove(uniqueCode);
    }

    public void addDownloadResult(String deviceUniqueCode, String uniqueCode, String result){
        String key = RedisKeys.isDownPrefix + redisUtil.getKey(deviceUniqueCode, uniqueCode);
        redisUtil.stringSet(key, result);
    }

    public DownloadAuthorityData getDownloadAuthorityData(String uniqueCode) {
        String key = RedisKeys.personInfoUniqueCodePrefix + uniqueCode;
        if (!redisUtil.hasKey(key)) return null;
        String personInfoJsonStr = redisUtil.stringGet(key);
        return JsonUtils.parseObject(personInfoJsonStr, DownloadAuthorityData.class);
    }
}