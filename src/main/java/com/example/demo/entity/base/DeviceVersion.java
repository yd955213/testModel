package com.example.demo.entity.base;

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
public class DeviceVersion {
    public String deviceUniqueCode;
    public String softwareVersion;
    public String lastTimeSoftwareVersion;
    public String softwareVersionUpdateTime;
    public String hardwareVersion;
    public String lastTimeHardwareVersion;
    public String hardwareVersionUpdateTime;
}
