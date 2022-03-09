package com.example.demo.service.init;

import com.example.demo.entity.api.DownloadAuthorityData;
import com.example.demo.entity.base.DownloadCount;
import com.example.demo.entity.base.DownloadFailMassageCount;
import com.example.demo.entity.base.DownloadMassage;
import com.example.demo.entity.commonInterface.GlobalVariable;
import com.example.demo.entity.commonInterface.RedisKeys;
import com.example.demo.utils.FileUtil;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.photo.ImageTools;
import com.example.demo.utils.redis.RedisUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 初始化人员信息
 * @modifiedBy:
 */
@Log4j2
@Component
public class PersonInfoManage {
    @Value("${picture.filePath}")
    private String photoPath;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    DeviceInfoDao deviceInfoDao;
    @Autowired
    PersonInfoDao personInfoDao;

    ImageTools imageTools = new ImageTools();

    //
    private final static String photoPathTemp = "data/originalPhoto/";

    /**
     * 根据 photoPath 目录下的照片的文件名，新建 人员信息map
     */
    public void init() {

        if (photoPath == null) photoPath = photoPathTemp;
        File file = new File(photoPath);
        if (file.exists()) {
            photoPath = file.getAbsolutePath();
        } else {
            photoPath = new File(photoPathTemp).getAbsolutePath();
        }

        List<String> allFilePathsInDirectory = null;
        try {
            allFilePathsInDirectory = imageTools.getAllFilePathsInDirectory(photoPath);
        } catch (FileNotFoundException e) {
            log.error("人员初始化操作，目录路径错误：{}", photoPath);
        }
        if (ObjectUtils.isEmpty(allFilePathsInDirectory)) return;
        /*
        人数太多时， 使用线程池去初始化
        每个线程 负责  once = 100 张照片转换为base64;
         */
        ExecutorService pool = new ThreadPoolExecutor(3, 6, 1000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        int temp = 0;
        int once = 100;
        CountDownLatch countDownLatch = new CountDownLatch(allFilePathsInDirectory.size() / once);
        int downCount;
        while (temp < allFilePathsInDirectory.size()) {
            int i = 0;
            List<String> filepathList = new ArrayList<>();
            List<String> uniqueCodeList = new ArrayList<>();
            if (temp + once <= allFilePathsInDirectory.size()) {
                downCount = once;
            } else {
                downCount = allFilePathsInDirectory.size() - temp;
            }
            while (i < downCount) {
                filepathList.add(allFilePathsInDirectory.get(temp));
                temp++;
                i++;
                uniqueCodeList.add(Integer.toString(temp));
            }
            pool.execute(() -> {
                // 生成权限下载的 数据 并写redis
                imageToDownloadAuthorityDataBean(filepathList, uniqueCodeList);
                // 线程执行完成，门闩 减一
                countDownLatch.countDown();
            });
        }
        // 等待所以子线程执行完成
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 刷新 本机缓存数据
        personInfoDao.refreshPersonUniqueCodeSet();
        personInfoDao.getNotDownloadedUniqueCodeMap();
    }

    public Set<String> getPersonInfoUniqueCodeSet() {
        return personInfoDao.getPersonInfoUniqueCodeSet();
    }

    public int getUniqueCodeSetSize() {
        return redisUtil.getSetSize(RedisKeys.personInfoUniqueCodeSet);
    }


    /**
     * 生成权限下载的 数据 并写redis
     *
     * @param filepathList filepathList
     */
    private void imageToDownloadAuthorityDataBean(List<String> filepathList, List<String> uniqueCodeList) {

        for (int i = 0; i < filepathList.size(); i++) {
            imageToDownloadAuthorityDataBean(filepathList.get(i), uniqueCodeList.get(i));
        }
    }

    private void imageToDownloadAuthorityDataBean(String filepath, String uniqueCode) {
        String imageToBase64 = imageTools.imageToBase64(filepath);
        if (!ObjectUtils.isEmpty(imageToBase64)) {
            String fileName = new File(filepath).getName().split("\\.")[0];
            DownloadAuthorityData downloadAuthorityData = new DownloadAuthorityData();
            downloadAuthorityData.setUniqueCode(uniqueCode);
            downloadAuthorityData.setStartTime(MyLocalTimeUtil.getLocalDataTime());
            downloadAuthorityData.setPersonNo(fileName);
            downloadAuthorityData.setPersonName(fileName);
            downloadAuthorityData.setPhoto(imageToBase64);
            // Redis 写数据
            String personInfoUniqueCodeKey = RedisKeys.personInfoUniqueCodePrefix + uniqueCode;
            // json 格式字符串
            redisUtil.stringSet(personInfoUniqueCodeKey, JsonUtils.toJsonStringNotNull(downloadAuthorityData, DownloadAuthorityData.class));
            redisUtil.setAdd(RedisKeys.personInfoUniqueCodeSet, uniqueCode);
            redisUtil.stringSet(RedisKeys.personPictureUri + uniqueCode, filepath);
        }
    }

    public List<String> getAllDownloadFailPicture() {
        Set<String> downloadFailUniqueCodeSet = getDownloadFailUniqueCodeSet();
        List<String> filePathList = new ArrayList<>();
        downloadFailUniqueCodeSet.forEach(uniqueCode -> {
            if (redisUtil.hasKey(RedisKeys.personPictureUri + uniqueCode)) {
                filePathList.add(redisUtil.stringGet(RedisKeys.personPictureUri + uniqueCode));
            }
        });
        return filePathList;
    }

    /**
     * 获取权限下载的有效数据 list 长度为 count
     *
     * @param count 需要下载长度
     */
    public List<DownloadAuthorityData> getDownloadAuthorityDataList(String mac, int count) {
        Set<String> downloadedUniqueCodeSet = personInfoDao.getNotDownloadedUniqueCodeMap().get(mac);
        if(ObjectUtils.isEmpty(downloadedUniqueCodeSet)) return new ArrayList<>();
        Set<String> uniqueCodeSet = new HashSet<>();
        for (String uniqueCode: downloadedUniqueCodeSet){
            uniqueCodeSet.add(uniqueCode);
            if(uniqueCodeSet.size() >= count) break;
        }

        List<DownloadAuthorityData> downloadAuthorityDataList = new ArrayList<>();
        uniqueCodeSet.forEach(uniqueCode -> {
            String jsonString = redisUtil.stringGet(RedisKeys.personInfoUniqueCodePrefix + uniqueCode);
            DownloadAuthorityData downloadAuthorityData = JsonUtils.parseObject(jsonString, DownloadAuthorityData.class);
            downloadAuthorityDataList.add(downloadAuthorityData);
        });
        return downloadAuthorityDataList;
    }

    public List<DownloadAuthorityData> getDownloadAuthorityDataList(String mac, String uniqueCode) {

        List<DownloadAuthorityData> downloadAuthorityDataList = new ArrayList<>();
        String jsonString = redisUtil.stringGet(RedisKeys.personInfoUniqueCodePrefix + uniqueCode);
        if(!ObjectUtils.isEmpty(jsonString)){
            DownloadAuthorityData downloadAuthorityData = JsonUtils.parseObject(jsonString, DownloadAuthorityData.class);
            downloadAuthorityDataList.add(downloadAuthorityData);
        }
        return downloadAuthorityDataList;
    }

    /**
     * 当前正在下载的人员，标记为 已下载，但设备未返回权限处理结果处理，
     *
     * @param deviceUnicode  设备mac
     * @param uniqueCodeList uniqueCodeList
     */
    public void downLoading(String deviceUnicode, List<String> uniqueCodeList) {
        uniqueCodeList.forEach(uniqueCode -> {
            redisUtil.stringSet(RedisKeys.isDownPrefix + redisUtil.getKey(deviceUnicode, uniqueCode), "1");
            String msg = "已下载，未返回";
            redisUtil.stringSet(RedisKeys.downloadMessagePrefix + redisUtil.getKey(deviceUnicode, uniqueCode), msg);
            redisUtil.setAdd(RedisKeys.downloadMessageSet, msg);
            redisUtil.increment(RedisKeys.downloadMessageCountPrefix + msg, Integer.toUnsignedLong(uniqueCodeList.size()));
            redisUtil.setAdd(RedisKeys.downloadedUniqueCodeSet + deviceUnicode, uniqueCode);
            // 将正在下载的人员 添加到 下载失败集合， 如果下载成功 则移除， 防止 下载权限给设备 设备在处理过程中重启 导致这批数据记录 未统计到失败记录中
            downloadFail(deviceUnicode, uniqueCode, msg);
        });
    }
    /**
     * 修改下载成功的 下载状态
     * @param mac        设备mac
     * @param uniqueCode 人员编号
     * @param msg        下载成功信息
     */
    public void downloadSuccess(String mac, String uniqueCode, String msg) {
        redisUtil.setAdd(RedisKeys.downloadSuccessUniqueCodeSet, uniqueCode);
        redisUtil.stringSet(RedisKeys.downloadMessagePrefix + redisUtil.getKey(mac, uniqueCode), msg);
        redisUtil.setAdd(RedisKeys.downloadSuccessSetByMacPrefix + mac, uniqueCode);
        redisUtil.setAdd(RedisKeys.downloadMessageSet, msg);
        personInfoDao.getDownloadSuccessUniqueCodeSet().add(uniqueCode);
        /*
        移除 downLoading 方法中 调用downloadFail 方法 产生的数据
         */
        redisUtil.setRemove(RedisKeys.downloadFailUniqueCodeSet, uniqueCode);
        redisUtil.setRemove(RedisKeys.downloadFailSetByMacPrefix + mac, uniqueCode);
        personInfoDao.getDownloadFailUniqueCodeSet().remove(uniqueCode);

        // 移除 未下载的人员集合
        personInfoDao.removeNotDownloadedUniqueCodeSet(mac, uniqueCode);

        addDownloadResult(mac, uniqueCode, "1");

    }

    /**
     * 修改下载失败的 下载状态
     *
     * @param mac        设备mac
     * @param uniqueCode 人员编号
     * @param msg        下载成功信息
     */
    public void downloadFail(String mac, String uniqueCode, String msg) {
        redisUtil.setAdd(RedisKeys.downloadFailUniqueCodeSet, uniqueCode);
        redisUtil.stringSet(RedisKeys.downloadMessagePrefix + redisUtil.getKey(mac, uniqueCode), msg);
        redisUtil.setAdd(RedisKeys.downloadFailSetByMacPrefix + mac, uniqueCode);
        redisUtil.setAdd(RedisKeys.downloadMessageSet, msg);

        getDownloadFailUniqueCodeSet().add(uniqueCode);
        addDownloadResult(mac, uniqueCode, "1");
        // 移除 未下载的人员集合
        personInfoDao.removeNotDownloadedUniqueCodeSet(mac, uniqueCode);
    }

    /**
     * 当有设备有处理权限通知上报 不管是否成功， 默认的 "已下载，未返回" 数量 减 1；
     */
    public void updateDownLoadMassageCount(String deviceUnicode, String uniqueCode) {
        String msg = "已下载，未返回";
        if (redisUtil.hasKey(RedisKeys.downloadMessagePrefix + redisUtil.getKey(deviceUnicode, uniqueCode))) {
            redisUtil.increment(RedisKeys.downloadMessageCountPrefix + msg, -1L);
        }
    }
    public void addDownloadResult(String deviceUniqueCode, String uniqueCode, String result){
        personInfoDao.addDownloadResult(deviceUniqueCode, uniqueCode, result);
    }


    public void addNotDownloadedUniqueCodeSet(String deviceUniqueCode, String uniqueCode) {
        personInfoDao.addNotDownloadedUniqueCodeSet(deviceUniqueCode, uniqueCode);
    }


    public void cleanDownloadSuccessUniqueCodeSet() {
        personInfoDao.getDownloadSuccessUniqueCodeSet().clear();
    }

    public void cleanDownloadFailUniqueCodeSet() {
        personInfoDao.getDownloadFailUniqueCodeSet().clear();

    }

    public String getDownloadFailCount(String mac) {
        return redisUtil.getSetSize(RedisKeys.downloadFailSetByMacPrefix + mac) + "";
    }

    public String getDownloadSuccessCount(String mac) {
        return redisUtil.getSetSize(RedisKeys.downloadSuccessSetByMacPrefix + mac) + "";
    }

    /**
     * 获取每台设备 权限下载失败 信息 统计数量
     *
     * @param mac mac
     * @return jsonString
     */
    public String getDownloadFailCountByMassage(String mac) {
        if (!redisUtil.hasKey(RedisKeys.downloadFailUniqueCodeSet)) return null;

        Set<String> downloadFailUniqueCodeSet = getDownloadFailUniqueCodeSet();
        List<String> msgList = new ArrayList<>();
        downloadFailUniqueCodeSet.forEach(uniqueCode -> {
            String key = RedisKeys.downloadMessagePrefix + redisUtil.getKey(mac, uniqueCode);
            if (redisUtil.hasKey(key)) {
                msgList.add(redisUtil.stringGet(key));
            }
        });

        HashMap<String, Integer> msgHashmap = new HashMap<>();
        msgList.forEach(msg -> {
            Integer count = msgHashmap.get(msg);
            msgHashmap.put(msg, count == null ? 1 : count + 1);
        });

        List<DownloadFailMassageCount.MassageCount> list = new ArrayList<>();
        msgHashmap.keySet().forEach(msg -> {
            DownloadFailMassageCount.MassageCount massageCount = new DownloadFailMassageCount.MassageCount();
            massageCount.setMassage(msg);
            massageCount.setCount(msgHashmap.get(msg));
            list.add(massageCount);
        });

        DownloadFailMassageCount downloadFailMassageCount = new DownloadFailMassageCount();
        downloadFailMassageCount.setDeviceUniqueCode(mac);
        downloadFailMassageCount.setList(list);

        return JsonUtils.toJsonStringWithNull(downloadFailMassageCount, DownloadFailMassageCount.class);
    }

    /**
     * 获取 所有设备下载权限 的统计信息
     *   {
     *   "deviceUniqueCode":"FFFFFF",
     *   successCount: 123 ,
     *   failCount: 123
     *   }
     * @return jsonString
     */
    public String getAllDownloadInfoCount() {
        List<DownloadCount> downloadCountList = new ArrayList<>();
        deviceInfoDao.getDeviceUniqueCodeSet().forEach(mac->{
            DownloadCount downloadCount = new DownloadCount();
            downloadCount.setDeviceUniqueCode(mac);
            downloadCount.setAuthorityCount(personInfoDao.getPersonInfoUniqueCodeSet().size());
            downloadCount.setHasNotDownloadCount(personInfoDao.getNotDownloadedUniqueCodeSetSize(mac));
            downloadCount.setDownloadedCount(redisUtil.getSetSize(RedisKeys.downloadedUniqueCodeSet + mac));
            downloadCount.setFailCount(redisUtil.getSetSize(RedisKeys.downloadFailSetByMacPrefix + mac));
            downloadCount.setSuccessCount(redisUtil.getSetSize(RedisKeys.downloadSuccessSetByMacPrefix + mac));
            downloadCountList.add(downloadCount);
        });
        return JsonUtils.toJsonStringWithNull(downloadCountList, new TypeToken<List<DownloadCount>>(){}.getType());
    }


    public String getAllDownloadAuthorityFailMassage() {
//        Set<String> downloadFailUniqueCodeSet = getDownloadFailUniqueCodeSet();

        Set<String> deviceInfoSet = deviceInfoDao.getDeviceUniqueCodeSet();
        List<DownloadMassage> downloadMassageList = new ArrayList<>();
        deviceInfoSet.forEach(mac -> {
            DownloadMassage downloadMassage = new DownloadMassage();
            downloadMassage.setDeviceUniqueCode(mac);
            List<DownloadMassage.Massage> massageList = new ArrayList<>();
            Set<String> downloadFailSet = redisUtil.setGet(RedisKeys.downloadFailSetByMacPrefix + mac);
            downloadFailSet.forEach(uniqueCode -> {
                if (redisUtil.hasKey(RedisKeys.downloadMessagePrefix + redisUtil.getKey(mac, uniqueCode))) {
                    DownloadMassage.Massage massage = new DownloadMassage.Massage();
                    massage.setMassage(getDownloadAuthorityMessage(mac, uniqueCode));
                    massage.setUniqueCode(uniqueCode);
                    massage.setName(getDownloadAuthorityData(uniqueCode).getPersonName());
                    massageList.add(massage);
                }
            });
            downloadMassage.setMassageList(massageList);
            downloadMassageList.add(downloadMassage);
        });
        return JsonUtils.toJsonStringWithNull(downloadMassageList, new TypeToken<List<DownloadMassage>>() {
        }.getType());
    }

    public String getDownloadAuthorityMessage(String mac, String uniqueCode) {
        return redisUtil.stringGet(RedisKeys.downloadMessagePrefix + redisUtil.getKey(mac, uniqueCode));
    }

    public DownloadAuthorityData getDownloadAuthorityData(String uniqueCode) {
        return personInfoDao.getDownloadAuthorityData(uniqueCode);
    }


    public Set<String> getDownloadFailUniqueCodeSet() {
        return personInfoDao.getDownloadFailUniqueCodeSet();
    }

    public Set<String> getDownloadSuccessUniqueCodeSet() {
        return personInfoDao.getDownloadSuccessUniqueCodeSet();
    }
    public Set<String> getDownloadedUniqueCodeSet() {
        return personInfoDao.getDownloadedUniqueCodeSet();
    }

    public String zipAllDownloadFailPicture(){
        String zipFilePath = new File("").getAbsolutePath() + "/" + GlobalVariable.errorPictureFilePath;
        File file = new File(zipFilePath);
        if (!file.exists()) {
            List<String> failPicturePathList = getAllDownloadFailPicture();
            List<File> personList = new ArrayList<>();
            failPicturePathList.forEach(filePath -> personList.add(new File(filePath)));
            zipFilePath = new FileUtil().toZip(personList, null);
        }
        return zipFilePath;
    }
}
