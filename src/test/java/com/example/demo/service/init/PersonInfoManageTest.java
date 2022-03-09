package com.example.demo.service.init;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: yd
 * @date: 2022-02-16
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@SpringBootTest
class PersonInfoTest {
    @Autowired
    PersonInfo personInfo;

    @Test
    void init() {
        personInfo.init();
        System.out.println("人数：" + PersonInfo.getPersonInfoMap().size());
    }
}