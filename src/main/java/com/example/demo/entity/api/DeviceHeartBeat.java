package com.example.demo.entity.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class DeviceHeartBeat {

    public String DeviceTime;
    public Integer AuthorityCount;
    public Integer UnuploadRecordsCount;
    public Integer DevicePort;
    public String ServerTime;
}
