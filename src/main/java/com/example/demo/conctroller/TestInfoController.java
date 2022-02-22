package com.example.demo.conctroller;

import com.example.demo.service.impl.TestInfoServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@RestController
@Log4j2
public class TestInfoController {
    @Autowired
    TestInfoServiceImpl testInfoService;

    @GetMapping("/testInfo")
    public String test(){
        return testInfoService.getTestInfo();
    }

    @GetMapping("/downloadAuthority")
    public String downloadAuthority(){
        return testInfoService.downloadAuthority();
    }

    @GetMapping("/downErrorInfo/{dev}")
    public String downErrorInfo(@PathVariable String dev){
        return testInfoService.downErrorInfo(dev);
    }

    @GetMapping("/ClearAllDeviceData")
    public String clearAllDeviceData(){
        return testInfoService.clearAllDeviceData();
    }

    @GetMapping("/downAuthorityErrorInfo")
    public ResponseEntity<FileSystemResource> downAuthorityErrorInfo() {return testInfoService.downAuthorityErrorInfo();}
}
