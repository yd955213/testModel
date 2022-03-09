package com.example.demo.entity.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-03-04
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class UploadDoorStatus {
    /**
     * 锁的状态     0：关闭 1：开门
     */
    public Integer Lock;
    /**
     * 按键状态     0：未按下 1：按键开门
     */
    public Integer Button;
    /**
     * 门磁状态     0：关闭 1：开门
     */
    public Integer GateMagnetism;
    /**
     * 消防报警状态   0：未报警 1：报警
     */
    public Integer Alarm;
}
