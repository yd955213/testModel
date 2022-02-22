package com.example.demo.entity.base;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: yd
 * @date: 2022-02-15
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@SpringBootTest
class SshUserTest {

    @Autowired
    SshUser sshUser;

    @Test
    void testToString() {
        System.out.println(sshUser.toString());
    }
}