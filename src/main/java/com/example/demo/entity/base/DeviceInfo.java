package com.example.demo.entity.base;

import lombok.Data;
import org.springframework.stereotype.Repository;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description: 设备信息实体类
 * @modifiedBy:
 */
@Repository
@Data
public class DeviceInfo {
    public Integer devicePort;
    public String deviceIp;
    public String deviceUniqueCode;
    public String timeStamp;
    public Long currentTime;
}
