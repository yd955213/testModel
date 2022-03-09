package com.example.demo.service.impl;


import com.example.demo.SchedulingTask.CommonTask;
import com.example.demo.entity.api.ClearDeviceData;
import com.example.demo.entity.api.DownloadAuthorityData;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.commonInterface.GlobalVariable;
import com.example.demo.service.TestInfoService;
import com.example.demo.service.init.*;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.redis.RedisUtil;
import com.example.demo.utils.restTemplateUtil.RestTemplateUtil;
import com.example.demo.utils.restTemplateUtil.UrlUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @author: yd
 * @date: 2022-02-16
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@Service("testInfoService")
@Slf4j
public class TestInfoServiceImpl implements TestInfoService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    DeviceInfoManage deviceInfoManage;
    @Autowired
    PersonInfoManage personInfoManage;

    @Override
    public String getTestInfo() {
        return "<p>" + CommonTask.getTestInfo() + "</p>" +
                "<p>" + CommonTask.getDownInfo() + "</p>" +
                "<p>" + "设备上报记录数：" + deviceInfoManage.getAllRecordCount() + "</p>" +
                "<p>" + "权限对比：" + CommonTask.getContrastMassage()+ "</p>" +
                "<p>" + "设备定时重启次数：" + deviceInfoManage.getALLDeviceRebootCount() + "</p>" +
                "<p>" + "权限下载错误信息：" + personInfoManage.getAllDownloadAuthorityFailMassage() + "</p>";
    }

    @Autowired
    ServerApiServiceImpl serverApiServiceImpl;

    @Override
    public String downloadAuthority() {
        // 由于进行权限下载时，设备不及时返回，这里开一个线程去处理
        new Thread(() -> deviceInfoManage.getDeviceUniqueCodeSet().forEach(mac -> serverApiServiceImpl.downloadAuthority(mac))).start();
        return "ok";
    }

    @Override
    public String downErrorInfo(String dev) {

        return personInfoManage.getAllDownloadAuthorityFailMassage();
    }

    @Autowired
    ClearDeviceData clearDeviceData;
    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Override
    public String clearAllDeviceData() {
        Set<String> deviceUniqueCodeSet = deviceInfoManage.getDeviceUniqueCodeSet();
        deviceUniqueCodeSet.forEach(mac -> {
            clearDeviceData.setClearLog("Y");
            clearDeviceData.setClearPerson("Y");
            clearDeviceData.setClearPassRecord("N");

            Request<ClearDeviceData> clearDeviceDataRequest = new Request<>();
            clearDeviceDataRequest.setData(clearDeviceData);
            clearDeviceDataRequest.setTimeStamp(MyLocalTimeUtil.getLocalDataTime());
            clearDeviceDataRequest.setDeviceUniqueCode(mac);

            String url = UrlUtil.getUrl(
                    deviceInfoManage.getDeviceIp(mac),
                    deviceInfoManage.getDevicePort(mac),
                    FaceDeviceApiUri.clearDeviceData
            );
            restTemplateUtil.post(url,
                    JsonUtils.toJsonStringNotNull(clearDeviceDataRequest, new TypeToken<Request<ClearDeviceData>>() {
                    }.getType()));
        });
        return "ok";
    }

    @Value("${picture.filePath}")
    String filePath;

    @Override
    public ResponseEntity<FileSystemResource> downAuthorityErrorInfo() {
        String zipFilePath = personInfoManage.zipAllDownloadFailPicture();
        return restTemplateUtil.upload(zipFilePath);
    }

    @Override
    public String clearDownloadedAuthorityAll() {
        deviceInfoManage.clearDownloadedAuthorityAll();
        return "ok";
    }

    @Override
    public String clearDownloadedAuthority(String dev) {
        deviceInfoManage.clearDownloadedAuthority(dev);
        return "ok";
    }
    @Autowired
    PersonInfoDao personInfoDao;
    @Autowired
    DeviceInfoDao deviceInfoDao;
    @Override
    public String refreshCacheData(){
        String msg = "ok";
        try {
            deviceInfoDao.init();
            personInfoDao.init();
        }catch (Exception e){
            msg = e.getMessage();
        }
        return  msg;
    }

    @Override
    public String downloadAuthorityByUserName(String deviceUniqueCode, String uniqueCode) {

        DownloadAuthorityData downloadAuthorityData = personInfoDao.getDownloadAuthorityData(uniqueCode);
        List<DownloadAuthorityData> downloadAuthorityDataList = Arrays.asList(downloadAuthorityData);

        Request<List<DownloadAuthorityData>> requestParamsBean = new Request<>();
        requestParamsBean.setDeviceUniqueCode(deviceUniqueCode);
        requestParamsBean.setTimeStamp(MyLocalTimeUtil.getLocalDataTime());
        requestParamsBean.setData(downloadAuthorityDataList);

        String requestParam = JsonUtils.toJsonStringNotNull(requestParamsBean,
                new TypeToken<Request<List<DownloadAuthorityData>>>() {
                }.getType());

        String deviceIp = deviceInfoManage.getDeviceIp(deviceUniqueCode);
        String devicePort = deviceInfoManage.getDevicePort(deviceUniqueCode);

        String url = UrlUtil.getUrl(deviceIp,devicePort,
                FaceDeviceApiUri.downloadAuthorityData);
        log.info("下载人员身份数据：Url={}, 数据长度：{}", url, requestParam.length());
        restTemplateUtil.post(url, requestParam);
        return requestParam;
    }
//    @Override
//    public ModelAndView showImage(){
//        File file = new File(GlobalVariable.originalPhotoFilePath);
//    }
}
