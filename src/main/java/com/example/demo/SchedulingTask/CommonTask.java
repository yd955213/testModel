package com.example.demo.SchedulingTask;

import com.alibaba.fastjson.TypeReference;
import com.example.demo.configs.DevPropertiesConfig;
import com.example.demo.driver.SSHConnect;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.api.Response;
import com.example.demo.entity.base.DeviceInfo;
import com.example.demo.service.impl.ServerApiServiceImpl;
import com.example.demo.service.init.DeviceInfoMap;
import com.example.demo.service.init.FaceDeviceApiUri;
import com.example.demo.service.init.PersonInfo;
import com.example.demo.utils.FileUtil;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.restTemplateUtil.UrlUtil;
import com.example.demo.utils.dingDing.DingDingMessageUtil;
import com.example.demo.utils.restTemplateUtil.HttpHeadersUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: yd
 * @date: 2022-02-14
 * @version: 1.0
 * @description: 每15分钟定时下载人脸权限
 * @modifiedBy:
 */

@Component
@Log4j2
public class CommonTask {
    public static String testInfo;
    public static String downInfo;
    public static String contrastMsg = "权限对比定时任务未开始";
    public String defaultTestInfo = "每60秒刷新显示 离线设备数：%s，离线设备mac：%s；  在线设备数：%s，在线设备mac：%s, ";
    /**
     * 设备共20台：
     * 对照组：5台，只下载一次权限，之后待机；
     * 识别组1：5台，只下载一次权限，一直进行人脸识别；
     * 识别组2：5台，一直进行人脸识别，且15分钟下载一次权限；
     * 识别组3：5台，一直进行人脸识别，10分钟重启一次；
     */
//    @Value("${server.port}")
//    public String port;

    @Autowired
    DevPropertiesConfig devPropertiesConfig;

    @Autowired
    ServerApiServiceImpl serverApiServiceImpl;
    // cron表达式 每15分钟执行一次
    @Scheduled(cron = "0 */15 * * * ?")
    public void start() {

        log.info("定时任务：每15分钟进行一次权限下载");
        DeviceInfoMap.setDownloadMapTrue(devPropertiesConfig.getRecognitionAndDownloadAuthorityGroup());
//        DeviceInfoMap.setDownloadMapTrue(devPropertiesConfig.getOpenAndCloseGroup());
        /*
        由于设备下载没有主动上包 权限更新通知，这改为主动下载
         */
        devPropertiesConfig.getRecognitionAndDownloadAuthorityGroup().forEach(mac -> serverApiServiceImpl.downloadAuthority(mac));
    }

    @Value("${reboot.type}")
    String rebootType;

    @Autowired
    SSHConnect sshConnect;

//        @Scheduled(cron="0 0 */1 * * ?")
    @Scheduled(cron = "0 */10 * * * ?") // 测试
    public void reboot() {

        //使用adb 命令方式进行重启，需要设备支持adb
        final String cmdCommand = "cmd /c start adb connect %s:5555 && adb -s %s:5555 reboot";
//        System.out.println(devPropertiesConfig.getOpenAndCloseGroup());
        devPropertiesConfig.getOpenAndCloseGroup().forEach(mac -> {
            DeviceInfo deviceInfo = DeviceInfoMap.getDeviceInfoMap().get(mac);
            if (deviceInfo != null) {
                String ip = deviceInfo.getDeviceIp();
                if ("adb".equals(rebootType)) {
                    String format = String.format(cmdCommand, ip, ip);
                    log.info("定时任务：每1个小时进行一次重启，adb命令：{}，设备IP：{}", format, ip);

                    try {
                        Runtime.getRuntime().exec(format);
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.info("执行window批处理命令失败，命令：" + format);
                    }
                } else {
                    // 使用ssh连接方式进行重启
                    // 测试时 发现ssh连接耗时最少10s 这里开一个线程去执行
                    new Thread(() -> {
                        log.info("定时任务：每1个小时进行一次重启，adb命令：reboot，设备IP：{}", ip);
                        sshConnect.createSshConnect(ip);
                        sshConnect.executeCommand("reboot");
                        sshConnect.disconnect();
                    }).start();
                }
                DeviceInfoMap.deviceRebootCount(mac);
            }
        });
    }

    @Scheduled(cron = "0 */1 * * * ?")
//    @Scheduled(cron = "0/10 * * * * ?")
    public void show() {

        System.out.println(DeviceInfoMap.getRecordsHashMap());
        Map<String, DeviceInfo> deviceInfoMap = DeviceInfoMap.getDeviceInfoMap();
        // 防止删除在线设备
        Set<String> onlineSet = new HashSet<>(deviceInfoMap.keySet());
        Set<String> offSet = DeviceInfoMap.getOffSet();
        long now = System.currentTimeMillis();
//        System.out.println("onlineSet = " + onlineSet);
        onlineSet.forEach(mac -> {
            DeviceInfo deviceInfo = deviceInfoMap.get(mac);
//            // 1分钟没有上报心跳，则认为设备离线
            long current = (ObjectUtils.isEmpty(deviceInfo) || ObjectUtils.isEmpty(deviceInfo.getCurrentTime())) ? 0L: deviceInfo.getCurrentTime();

            if (now - current > 60000L) {
                DeviceInfoMap.addOfflineDev(mac);
            }
        });
        onlineSet.removeAll(offSet);
        DeviceInfoMap.setOnlineSet(onlineSet);
        testInfo = String.format(defaultTestInfo, offSet.size(), offSet, onlineSet.size(), onlineSet);

        HashMap<String, String> download = new HashMap<>();
        Map<String, Integer> downCountMap = PersonInfo.getDownCountMap();

        downCountMap.forEach((key, value) -> {
            String msg = "权限下载次数：%s, 失败数：%s";
            if (ObjectUtils.isEmpty(value)) {
                value = 0;
            }
            // 每台设备失败数集合
            HashMap<String, PersonInfo.ErrorPerson> errorPersonHashMap = PersonInfo.getDownErrorInfoMap().get(key);

            AtomicInteger tempCount = new AtomicInteger();
            errorPersonHashMap.forEach((uniqueCodeTemp, errorPerson) -> tempCount.addAndGet(errorPerson.getErrorCount()));
            int error = ObjectUtils.isEmpty(errorPersonHashMap) ? 0 : tempCount.get();
            msg = String.format(msg, value, error);
            download.put(key, msg);
            HashSet<String> nameSet = new HashSet<>();
            errorPersonHashMap.keySet().forEach( uniqueCodeTemp -> nameSet.add(errorPersonHashMap.get(uniqueCodeTemp).getUniqueCode()));
            log.info("下载权限失败人员姓名集合：" + nameSet);
        });
        downInfo = " 权限下载信息：" + download;
        log.info("定时任务,每1分钟刷新一次测试信息：{}", testInfo + downInfo);
    }


    @Autowired
    RestTemplate restTemplate;

    /**
     * 每50分钟进行一次权限对比  定时任务
     */
    @Scheduled(cron = "0 */50 * * * ?")
    public void contrast() {
        DeviceInfoMap.getDeviceInfoMap().forEach((mac, devInfo) -> {
            Request<String> request = new Request<>();
            request.setDeviceUniqueCode(mac);

            String requestParam = JsonUtils.toJsonStringWithNull(request, Request.class);


            HttpHeaders headers = HttpHeadersUtil.getHeaders();
            HttpEntity<String> httpEntity = new HttpEntity<>(requestParam, headers);
            // 4.14 获取所有人员信息
            String url = UrlUtil.getUrl(devInfo.getDeviceIp(), devInfo.getDevicePort(), FaceDeviceApiUri.getAllPerson);
            log.info(" 设备: {} 进行权限对比操作：Url={}, 数据：{}", mac, url, requestParam);
            ResponseEntity<String> responseEntity;
//            String msg = "";
            try {
                responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
                log.info("设备返回：{}", responseEntity.getBody());

                Response<List<String>> responseParam = JsonUtils.parseObject(
                        responseEntity.getBody(),
                        new TypeReference<Response<List<String>>>() {
                        }.getType());
                contrastMsg = String.format("权限对比:当前权限数：%s, 已下载权限数（去掉重复下载）：%s",
                        ObjectUtils.isEmpty(responseParam) ? 0 : (ObjectUtils.isEmpty(responseParam.getData()) ? 0 : responseParam.getData().size()),
                        PersonInfo.getDownPersonSet().size());

            } catch (Exception e) {
                contrastMsg = "权限对比: 设备无返回，暂不支持改接口，异常信息：" + e.getMessage();
            }
            DeviceInfoMap.getContrastMap().put(mac, contrastMsg);

        });
        log.info("定时任务：每50分钟进行一次权限对比：{}", DeviceInfoMap.getContrastMap());
    }

    @Scheduled(cron = "0 */30 * * * ?")
    public void writeFile() {
        String filePath = new File("").getAbsolutePath() + "/data/testLogs.txt";
//        System.out.println(filePath);
        FileUtil fileUtil = new FileUtil(filePath);
        fileUtil.write(testInfo);
        fileUtil.write(downInfo);
        fileUtil.write("记录上报：" + DeviceInfoMap.getRecordsHashMap().toString());
        fileUtil.write(PersonInfo.getDownErrorInfoMap().toString());
        log.info("定时任务：每30分将当前测试内容写文件，路径：{}，", filePath);
    }

    @Autowired
    DingDingMessageUtil dingDingMessageUtil;

    @Scheduled(cron = "0 30 17 * * ?")
    public void sendDingDingMassage() {
        String msg = "362老化测试-测试方法：" + "\r\n" +
                "  * 设备共20台：\n" +
                "  * 对照组：5台，只下载一次权限，之后待机；\n" +
                "  * 识别组1：5台，只下载一次权限，一直进行人脸识别；\n" +
                "  * 识别组2：5台，一直进行人脸识别，且15分钟下载一次权限；\n" +
                "  * 识别组3：5台，一直进行人脸识别，10分钟重启一次；"+"\r\n" + "\r\n" +
                "测试信息："+ "\r\n" +
                testInfo + "\r\n"  + "\r\n" +
                downInfo + "\r\n" + "\r\n" +
                contrastMsg + "\r\n" + "\r\n" +
                "设备上报记录数：" + DeviceInfoMap.getRecordsHashMap() + "\r\n" + "\r\n" +
                "设备定时重启次数：" + DeviceInfoMap.getDeviceRebootMap() + "\r\n" + "\r\n" +
                "公司局域网查询实时信息：" + "\r\n" +
                "在线状态：" + "http://172.168.120.230:19101/testInfo" + "\r\n" +
                "权限下载错误信息：" + "http://172.168.120.230:19101/downErrorInfo/设备mac地址" + "\r\n" +
                "权限下载照片：" + "http://172.168.120.230:19101/downAuthorityErrorInfo";
        log.info("定时任务：每天下午 17：30 向叮叮群助手发送消息：{}，", msg);
        dingDingMessageUtil.sendMassage(msg);
    }

    public static String getTestInfo() {
        return testInfo;
    }

    public static String getDownInfo() {
        return downInfo;
    }

//    public static String getContrastMsg() {
//        return contrastMsg;
//    }
}
