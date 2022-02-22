package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.SchedulingTask.CommonTask;
import com.example.demo.entity.api.ClearDeviceData;
import com.example.demo.entity.api.Request;
import com.example.demo.entity.base.DeviceInfo;
import com.example.demo.service.TestInfoService;
import com.example.demo.service.init.DeviceInfoMap;
import com.example.demo.service.init.FaceDeviceApiUri;
import com.example.demo.service.init.PersonInfo;
import com.example.demo.utils.FileUtil;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.restTemplateUtil.RestTemplateUtil;
import com.example.demo.utils.restTemplateUtil.UrlUtil;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;

/**
 * @author: yd
 * @date: 2022-02-16
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@Service("testInfoService")
public class TestInfoServiceImpl implements TestInfoService {
    @Override
    public String getTestInfo() {
        return "<p>" + CommonTask.getTestInfo() + "</p>" +
        "<p>" + CommonTask.getDownInfo() + "</p>" +
        "<p>" +  "设备上报记录数：" + DeviceInfoMap.getRecordsHashMap() + "</p>" +
        "<p>" + "权限对比：" + DeviceInfoMap.getContrastMap() + "</p>" +
        "<p>" + "设备定时重启次数：" + DeviceInfoMap.getDeviceRebootMap().toString() + "</p>" +
        "<p>" + "权限下载错误信息：" + PersonInfo.getDownErrorInfoMap().toString() + "</p>";
    }

    @Autowired
    ServerApiServiceImpl serverApiServiceImpl;

    @Override
    public String downloadAuthority() {
        // 由于进行权限下载时，设备不及时返回，这里开一个线程去处理
        new Thread(() -> DeviceInfoMap.getDeviceInfoMap().keySet().forEach(mac -> serverApiServiceImpl.downloadAuthority(mac))).start();
        return "ok";
    }

    @Override
    public String downErrorInfo(String dev) {

        Map<String, HashMap<String, PersonInfo.ErrorPerson>> downErrorInfoMap = PersonInfo.getDownErrorInfoMap();
        HashMap<String, PersonInfo.ErrorPerson> devErr = downErrorInfoMap.containsKey(dev) ? downErrorInfoMap.get(dev) : new HashMap<>();
        return JSONObject.toJSONString(devErr);
    }

    @Autowired
    ClearDeviceData clearDeviceData;
    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Override
    public String clearAllDeviceData() {
        Map<String, DeviceInfo> deviceInfoMap = DeviceInfoMap.getDeviceInfoMap();
        deviceInfoMap.forEach((mac, deviceInfo) ->{
            clearDeviceData.setClearLog("Y");
            clearDeviceData.setClearPerson("Y");
            clearDeviceData.setClearPassRecord("N");

            Request<ClearDeviceData> clearDeviceDataRequest = new Request<>();
            clearDeviceDataRequest.setData(clearDeviceData);
            clearDeviceDataRequest.setTimeStamp(MyLocalTimeUtil.getLocalDataTime());
            clearDeviceDataRequest.setDeviceUniqueCode(mac);

            String url = UrlUtil.getUrl(deviceInfo.getDeviceIp(), deviceInfo.getDevicePort(), FaceDeviceApiUri.clearDeviceData);
            restTemplateUtil.post(url, JsonUtils.toJsonStringNotNull(clearDeviceDataRequest, new TypeToken<Request<ClearDeviceData>>(){}.getType()));
        });
        return "ok";
    }

    @Value("${picture.filePath}")
    String filePath;
    @Override
    public ResponseEntity<FileSystemResource> downAuthorityErrorInfo() {

        String zipFilePath= new File("").getAbsolutePath() + "/data/权限下载失败照片.zip";
        File file = new File(zipFilePath);
        if(!file.exists()){
            Set<String> personSet = new HashSet<>();
            PersonInfo.getDownErrorInfoMap().forEach((mac, errorPersonHashMap) ->{
                errorPersonHashMap.forEach((uniqueCode, errorPerson) -> {
//                try {
//                    File file = ResourceUtils.getFile(filePath);
//                personSet.add(filePath+"\\" + errorPerson.getUniqueCode() + ".jpg");
                    personSet.add("E:\\02人脸测试111\\1人脸库\\真人 - 副本/" + errorPerson.getUniqueCode() + ".jpg");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
                });
            });
            List<File> personList = new ArrayList<>();
            personSet.forEach(filePath -> personList.add(new File(filePath)));
            zipFilePath = new FileUtil().toZip(personList, null);
        }

       return restTemplateUtil.upload(zipFilePath);
    }
}
