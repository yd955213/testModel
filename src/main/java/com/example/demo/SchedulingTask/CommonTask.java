package com.example.demo.SchedulingTask;

import com.alibaba.fastjson.TypeReference;
import com.example.demo.configs.DevPropertiesConfig;
import com.example.demo.driver.SSHConnect;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.api.Response;
import com.example.demo.entity.base.DownloadMassage;
import com.example.demo.entity.commonInterface.GlobalVariable;
import com.example.demo.service.impl.ServerApiServiceImpl;
import com.example.demo.service.init.*;
import com.example.demo.utils.FileUtil;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.restTemplateUtil.RestTemplateUtil;
import com.example.demo.utils.restTemplateUtil.UrlUtil;
import com.example.demo.utils.dingDing.DingDingMessageUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private static String testInfo;
    private static String downInfo;
//    private static String contrastMsgCount = "权限对比定时任务未开始";
    private static String contrastMassage = "权限对比定时任务未开始";
    private final static String defaultTestInfo = "每60秒刷新显示 离线设备数：%s，离线设备mac：%s；  在线设备数：%s，在线设备mac：%s, ";
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

    @Autowired
    DeviceInfoManage deviceInfoManage;
    @Autowired
    PersonInfoManage personInfoManage;

    // cron表达式 每15分钟执行一次
    @Scheduled(cron = "0 */15 * * * ?")
    public void downLoadAuthority() {

        log.info("定时任务：每15分钟进行一次权限下载");
        deviceInfoManage.setDownloadMapTrue(devPropertiesConfig.getRecognitionAndDownloadAuthorityGroup());
//        DeviceInfoMap.setDownloadMapTrue(devPropertiesConfig.getOpenAndCloseGroup());
        /*
        由于设备下载没有主动上包 权限更新通知，这改为主动下载
         */
        devPropertiesConfig.getRecognitionAndDownloadAuthorityGroup().forEach(mac -> serverApiServiceImpl.downloadAuthority(mac));
    }

    @Value("${reboot.type}")
    String rebootType;

//    @Autowired
//    SSHConnect sshConnect;

//        @Scheduled(cron="0 0 */1 * * ?")
    @Scheduled(cron = "0 */10 * * * ?") // 测试
    public void reboot() {

        //使用adb 命令方式进行重启，需要设备支持adb
        final String cmdCommand = "cmd /c start adb connect %s:5555 && adb -s %s:5555 reboot";
        devPropertiesConfig.getOpenAndCloseGroup().forEach(mac -> {
            System.out.println(deviceInfoManage);
            String ip = deviceInfoManage.getDeviceIp(mac);
            if(null == ip)  return;

            if ("adb".equals(rebootType)) {
                String format = String.format(cmdCommand, ip, ip);
                log.info("定时任务：每1个小时进行一次重启，adb命令：{}，设备IP：{}", format, ip);

                try {
                    Runtime.getRuntime().exec(format);
                    deviceInfoManage.setRebootDeviceTime(mac, MyLocalTimeUtil.getLocalDataTime());
                } catch (IOException e) {
                    log.info("执行window批处理命令失败，命令：{}，异常信息：{}", format, e.getMessage());
                }
            } else {
                // 使用ssh连接方式进行重启
                // 测试时 发现ssh连接耗时最少10s 这里开一个线程去执行
                new Thread(() -> {
                    log.info("定时任务：每1个小时进行一次重启，ssh命令：reboot，设备IP：{}", ip);
                    SSHConnect sshConnect = new SSHConnect();
                    try {
                        sshConnect.createSshConnect(ip);
                        sshConnect.executeCommand("reboot");
                        deviceInfoManage.setRebootDeviceTime(mac, MyLocalTimeUtil.getLocalDataTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("ssh 执行命令失败，详细信息：{} 或者 {}", e.getMessage(), e.getCause());
                    }
                    sshConnect.disconnect();
                }).start();
            }
                /*
                记录重启次数
                 */
//                deviceInfoManage.getALLDeviceRebootCount();
        });
    }

    @Scheduled(cron = "0 */1 * * * ?")
//    @Scheduled(cron = "0/10 * * * * ?")
    public void show() {
        /*
        检查设备是否在线
         */
        // 方法 getOnlineDevice getOfflineDevice 中已调用 checkedTheDeviceIsOnline 屏蔽
//        deviceInfoManage.checkedTheDeviceIsOnline();
        Set<String> onlineDevice = deviceInfoManage.getOnlineDevice();
        Set<String> offlineDevice = deviceInfoManage.getOfflineDevice();
        testInfo = String.format(defaultTestInfo, offlineDevice.size(), offlineDevice, onlineDevice.size(), onlineDevice);
        /*
        获取权限下载信息
         */

        downInfo = " 权限下载信息：" + personInfoManage.getAllDownloadInfoCount() + "  ";
        log.info("定时任务,每1分钟刷新一次测试信息：{}", testInfo + downInfo);
    }

    @Autowired
    RestTemplateUtil restTemplateUtil;
    /**
     * 每50分钟进行一次权限对比  定时任务
     */
    @Scheduled(cron = "0 */50 * * * ?")
    public void contrast() {
        Set<String> deviceUniqueCodeSet = deviceInfoManage.getDeviceUniqueCodeSet();
        List<DownloadMassage> downloadMassagesList = new ArrayList<>();
        deviceUniqueCodeSet.forEach(mac -> {
            Request<String> request = new Request<>();
            request.setDeviceUniqueCode(mac);

            String requestParam = JsonUtils.toJsonStringWithNull(request, Request.class);
            // 4.14 获取所有人员信息
            String url = UrlUtil.getUrl(deviceInfoManage.getDeviceIp(mac), deviceInfoManage.getDevicePort(mac), FaceDeviceApiUri.getAllPerson);
            log.info(" 设备: {} 进行权限对比操作：Url={}, 数据：{}", mac, url, requestParam);

            String responseParam = restTemplateUtil.post(url, requestParam);
            log.info("设备返回：{}", responseParam);

            if(!ObjectUtils.isEmpty(responseParam)){
                Response<List<String>> responseParamList = JsonUtils.parseObject(
                       responseParam,
                        new TypeReference<Response<List<String>>>() {
                        }.getType());
//                contrastMsgCount = String.format("权限对比:当前权限数：%s, 已下载权限数（去掉重复下载）：%s",
//                            ObjectUtils.isEmpty(responseParamList) ? 0 : (ObjectUtils.isEmpty(responseParamList.getData()) ? 0 : responseParamList.getData().size()),
//                            personInfoManage.getDownloadSuccessCount(mac));
                DownloadMassage downloadMassage;
                if(null != responseParamList.getData()){
                    downloadMassage = deviceInfoManage.doAuthorityContrast(mac, responseParamList.getData());
                }else {
                    downloadMassage = new DownloadMassage();
                    downloadMassage.setDeviceUniqueCode(mac);
                    DownloadMassage.Massage massage = new DownloadMassage.Massage();
                    massage.setMassage("设备未返回数据或者 data 为null");
                    downloadMassage.setMassageList(Collections.singletonList(massage));
                }
                downloadMassagesList.add(downloadMassage);
            }else {
                DownloadMassage downloadMassage = new DownloadMassage();
                downloadMassage.setDeviceUniqueCode(mac);
                DownloadMassage.Massage massage = new DownloadMassage.Massage();
                massage.setMassage("设备未返回数据或者 data 为null");
                downloadMassage.setMassageList(Collections.singletonList(massage));
                downloadMassagesList.add(downloadMassage);
            }
        });
        contrastMassage = JsonUtils.toJsonStringNotNull(downloadMassagesList, new TypeToken<List<DownloadMassage>>() {}.getType());

        System.out.println("contrastMassage" + contrastMassage);
        log.info("定时任务：每50分钟进行一次权限对比：{}", contrastMassage);
    }

    @Scheduled(cron = "0 */30 * * * ?")
    public void writeFile() {
        String filePath = new File("").getAbsolutePath() + "/data/testLogs.txt";
//        System.out.println(filePath);
        FileUtil fileUtil = new FileUtil(filePath);
        fileUtil.write(testInfo);
        fileUtil.write(downInfo);
        fileUtil.write("记录上报：" + deviceInfoManage.getAllRecordCount());
        fileUtil.write("权限下载错误" + personInfoManage.getAllDownloadAuthorityFailMassage());
        log.info("定时任务：每30分将当前测试内容写文件，路径：{}，", filePath);
    }

    @Autowired
    DingDingMessageUtil dingDingMessageUtil;

    @Scheduled(cron = "0 30 17 * * ?")
    public void sendDingDingMassage() {
        String msg = "364老化测试-测试方法：" + "\r\n" +
                "  * 设备共20台：\n" +
                "  * 对照组：5台，只下载一次权限，之后待机；\n" +
                "  * 识别组1：5台，只下载一次权限，一直进行人脸识别；\n" +
                "  * 识别组2：5台，一直进行人脸识别，且15分钟下载一次权限；\n" +
                "  * 识别组3：5台，一直进行人脸识别，10分钟重启一次；"+"\r\n" + "\r\n" +
                "测试信息："+ "\r\n" +
                testInfo + "\r\n"  + "\r\n" +
                downInfo + "\r\n" + "\r\n" +
                "权限对比： " + contrastMassage + "\r\n" + "\r\n" +
                "设备上报记录数：" + deviceInfoManage.getAllRecordCount() + "\r\n" + "\r\n" +
//                "设备定时重启次数：" + DeviceInfoManage.getDeviceRebootMap() + "\r\n" + "\r\n" +
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

    public static String getContrastMassage() {
        return contrastMassage;
    }

    @Autowired
    DeviceInfoDao deviceInfoDao;
    @Autowired
    PersonInfoDao personInfoDao;
    @Scheduled(cron = "0 0 */1 * * ?")
    public void refresh(){
        deviceInfoDao.init();
        personInfoDao.init();
    }

    /**
     * 定时任务：每天执行一次  重新生成昨天下载权限错误 的人的照片
     */
    @Scheduled(cron="0 0 0 1/1 * ?")
    public void errorPicture(){
        File file = new File(GlobalVariable.errorPictureFilePath);
        if(file.exists()) FileUtils.deleteQuietly(file);

        personInfoManage.zipAllDownloadFailPicture();
    }
}
