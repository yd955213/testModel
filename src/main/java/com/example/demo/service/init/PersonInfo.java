package com.example.demo.service.init;

import com.example.demo.entity.api.DownloadAuthorityData;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.MyLocalTimeUtil;
import com.example.demo.utils.photo.ImageTools;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 初始化人员信息
 * @modifiedBy:
 */
@Log4j2
public class PersonInfo {
    private String photoPath = "classpath:static/facePhotos";
    private final static Map<String, DownloadAuthorityData> personInfoMap = Collections.synchronizedMap(new HashMap<>());

    public static Map<String, DownloadAuthorityData> getPersonInfoMap() {
        return personInfoMap;
    }

    /**
     * 根据 photoPath 目录下的照片的文件名，新建 人员信息map
     */
    public void init(){
        ImageTools imageTools = new ImageTools();
        List<String> allFilePathsInDirectory =null;
        try {
            allFilePathsInDirectory = imageTools.getAllFilePathsInDirectory(photoPath);
        } catch (FileNotFoundException e) {
            log.error("人员初始化操作，目录路径错误：{}", photoPath);
        }
        if(ObjectUtils.isEmpty(allFilePathsInDirectory)) return;
        allFilePathsInDirectory.forEach(filepath -> {
            String fileName = new File(filepath).getName().split("\\.")[0];
            DownloadAuthorityData downloadAuthorityData = new DownloadAuthorityData();
            downloadAuthorityData.setUniqueCode(fileName);
            downloadAuthorityData.setStartTime(MyLocalTimeUtil.getLocalDataTime());
            downloadAuthorityData.setPersonNo(fileName);
            downloadAuthorityData.setPersonName(fileName);
            downloadAuthorityData.setPhoto(imageTools.imageToBase64(filepath));
            personInfoMap.put(downloadAuthorityData.getUniqueCode(), downloadAuthorityData);
        });
    }
}
