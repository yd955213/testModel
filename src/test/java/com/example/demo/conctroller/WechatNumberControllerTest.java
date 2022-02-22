package com.example.demo.conctroller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@SpringBootTest
class WechatNumberControllerTest {
    @Autowired
    WechatNumberController wechatNumberController;

    @Test
    void getAccessToken() {
        wechatNumberController.getAccessToken();
    }
}