package com.example.demo.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

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
}
