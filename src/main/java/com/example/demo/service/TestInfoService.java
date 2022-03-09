package com.example.demo.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author: yd
 * @date: 2022-02-16
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
public interface TestInfoService {
    String getTestInfo();
    String downloadAuthority();
    String downErrorInfo(String dev);
    String clearAllDeviceData();
    ResponseEntity<FileSystemResource> downAuthorityErrorInfo();
    String clearDownloadedAuthorityAll();
    String clearDownloadedAuthority(String dev);
    String refreshCacheData();
    String downloadAuthorityByUserName(String deviceUniqueCode, String uniqueCode);
//    ModelAndView showImage(){}
}
