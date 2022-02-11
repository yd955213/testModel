package com.example.demo.conctroller;

import com.example.demo.entity.api.Response;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@RestController
@Log4j2
@RequestMapping("/1")
public class TestController {
    @GetMapping("/test")
    public String test(){
        log.info("放行成功");
        return new Response<String>().toString();
    }
}
