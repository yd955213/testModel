package com.example.demo.service.init;

import com.example.demo.entity.api.DownloadAuthorityData;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.photo.ImageTools;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 初始化人员信息
 * @modifiedBy:
 */
@Log4j2
@Component
public class PersonInfo {
    @Value("${picture.filePath}")
    private  String photoPath;
    private final static String photoPathTemp = "classpath:static/facePhotos";
    private final static Map<String, DownloadAuthorityData> personInfoMap = Collections.synchronizedMap(new HashMap<>());
    private final static Map<String, Integer> downCountMap = Collections.synchronizedMap(new HashMap<>());
    // 根据设备存储下载权限失败的人员编号的集合 1分钟刷新1次
    private final static Map<String, Set<String>> downPersonSet = Collections.synchronizedMap(new HashMap<>());
    // 根据设备存储 下载权限失败 每个人员编号对应的失败信息
    private final static Map<String, HashMap<String, ErrorPerson>> downErrorInfoMap = Collections.synchronizedMap(new HashMap<>());

    private final static Set<String> uniqueCodeSet = Collections.synchronizedSet(new HashSet<>());

    public static Map<String, Set<String>> getDownPersonSet() {
        return downPersonSet;
    }

    /**
     * 根据 photoPath 目录下的照片的文件名，新建 人员信息map
     */
    public void init() {
        if (ObjectUtils.isEmpty(photoPath) || !(new File(photoPath).exists())) {
            photoPath = photoPathTemp;
        }
//        System.out.println(photoPath);
        ImageTools imageTools = new ImageTools();
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
        AtomicInteger atomicInteger = new AtomicInteger(0);
        while (temp < allFilePathsInDirectory.size()) {
            int i = 0;
            List<String> filepathList = new ArrayList<>();
            if (temp + once <= allFilePathsInDirectory.size()) {
                downCount = once;
            } else {
                downCount = allFilePathsInDirectory.size() - temp;
            }
            while (i < downCount) {
                filepathList.add(allFilePathsInDirectory.get(temp));
                temp++;
                i++;
            }
            pool.execute(() -> {
                filepathList.forEach(filepath -> {
                    String imageToBase64 = imageTools.imageToBase64(filepath);
                    if (!ObjectUtils.isEmpty(imageToBase64)) {
                        String fileName = new File(filepath).getName().split("\\.")[0];
                        //　使用　Set　集合保证 uniqueCode 唯一性
                        String uniqueCode = Integer.toString(new Random().nextInt(1000000));
                        while (uniqueCodeSet.contains(uniqueCode)) {
                            uniqueCode = Integer.toString(new Random().nextInt(1000000));
                        }
                        uniqueCodeSet.add(uniqueCode);
                        DownloadAuthorityData downloadAuthorityData = new DownloadAuthorityData();
                        downloadAuthorityData.setUniqueCode(uniqueCode);
                        downloadAuthorityData.setStartTime(MyLocalTimeUtil.getLocalDataTime());
                        downloadAuthorityData.setPersonNo(fileName);
                        downloadAuthorityData.setPersonName(fileName);
                        downloadAuthorityData.setPhoto(imageToBase64);
                        personInfoMap.put(downloadAuthorityData.getUniqueCode(), downloadAuthorityData);
                    }
                });
                // 线程执行完成，门闩 减一
                countDownLatch.countDown();
            });
        }
        // 等待所以子线程执行完成
        if (countDownLatch != null) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 权限下在 默认一次下载30个人， 设备处理成一个人化返回一次结果
     * 100人下载 设备概率 卡死 改为30人
     * 接口：/UploadAuthorityDealResult
     *
     * @param mac         设备mac
     * @param count       下载次数
     * @param downloadSet 单次下载的人数的 人员编号几何
     */
    public void addDownload(String mac, int count, Set<String> downloadSet) {
        if (downCountMap.containsKey(mac) && !ObjectUtils.isEmpty(downCountMap.get(mac))) {
            downCountMap.put(mac, downCountMap.get(mac) + count);
        } else {
            downCountMap.put(mac, count);
        }
        if (downPersonSet.containsKey(mac) && !ObjectUtils.isEmpty(downPersonSet.get(mac))) {
            downPersonSet.get(mac).addAll(downloadSet);
        } else {
            downPersonSet.put(mac, downloadSet);
        }

        downloadSet.forEach(uniqueCode -> {
            if (downErrorInfoMap.containsKey(mac) && !ObjectUtils.isEmpty(downErrorInfoMap.get(mac))) {
//                HashMap<String, ErrorPerson> macErrorPersonHashMap = downErrorInfoMap.get(mac);
                if (downErrorInfoMap.get(mac).containsKey(uniqueCode) && !ObjectUtils.isEmpty(downErrorInfoMap.get(mac).get(uniqueCode))) {
                    downErrorInfoMap.get(mac).get(uniqueCode).setErrorMsg("已下载，未返回");
                    downErrorInfoMap.get(mac).get(uniqueCode).setErrorCount(downErrorInfoMap.get(mac).get(uniqueCode).getErrorCount() + 1);
                } else {
                    ErrorPerson errorPerson = new ErrorPerson();
                    // 上个版本是uniqueCode ，这个版本修改为姓名
                    errorPerson.setUniqueCode(personInfoMap.get(uniqueCode).getPersonName());
                    errorPerson.setErrorMsg("已下载，未返回");
                    errorPerson.setErrorCount(1);
                    downErrorInfoMap.get(mac).put(uniqueCode, errorPerson);
                }
            } else {
                ErrorPerson errorPerson = new ErrorPerson();
                errorPerson.setUniqueCode(personInfoMap.get(uniqueCode).getPersonName());
                errorPerson.setErrorMsg("已下载，未返回");
                errorPerson.setErrorCount(1);
                HashMap<String, ErrorPerson> macErrorPersonHashMap = new HashMap<>();
                macErrorPersonHashMap.put(uniqueCode, errorPerson);
                downErrorInfoMap.put(mac, macErrorPersonHashMap);
            }
        });
    }


    public void downloadSuccess(String mac, String uniqueCode) {
        if (downErrorInfoMap.containsKey(mac) && !ObjectUtils.isEmpty(downErrorInfoMap.get(mac)) &&
                downErrorInfoMap.get(mac).containsKey(uniqueCode)) {
            downErrorInfoMap.get(mac).remove(uniqueCode);
        }
    }

    public static Map<String, Integer> getDownCountMap() {
        return downCountMap;
    }

    public static Map<String, DownloadAuthorityData> getPersonInfoMap() {
        return personInfoMap;
    }

    public static Map<String, HashMap<String, ErrorPerson>> getDownErrorInfoMap() {
        return downErrorInfoMap;
    }

    public static class ErrorPerson {
        public String name;
        public int errorCount;
        public String errorMsg;

        @Override
        public String toString() {
            return "ErrorPerson{" +
                    "name='" + name + '\'' +
                    ", errorCount=" + errorCount +
                    ", errorMsg='" + errorMsg + '\'' +
                    '}';
        }

        public String getUniqueCode() {
            return name;
        }

        public ErrorPerson setUniqueCode(String name) {
            this.name = name;
            return this;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public ErrorPerson setErrorCount(int errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public ErrorPerson setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
            return this;
        }
    }

    public static String getPhotoPathTemp() {
        return photoPathTemp;
    }

}
