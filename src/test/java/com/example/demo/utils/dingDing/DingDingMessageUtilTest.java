package com.example.demo.utils.dingDing;

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
class DingDingMessageUtilTest {
    @Autowired
    DingDingMessageUtil dingDingMessageUtil;

    @Test
    void sendMassage() {
        dingDingMessageUtil.sendMassage("测试一下");
    }
}