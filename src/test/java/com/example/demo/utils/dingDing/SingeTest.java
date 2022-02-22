package com.example.demo.utils.dingDing;

import org.junit.jupiter.api.Test;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
class SingeTest {

    @Test
    void encode() {
        new Sign().encode(System.currentTimeMillis());
    }
}