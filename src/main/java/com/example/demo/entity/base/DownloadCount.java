package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-03-01
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class DownloadCount {
    public String deviceUniqueCode;
    public Integer authorityCount;
    public Integer downloadedCount;
    public Integer hasNotDownloadCount;
    public Integer successCount;
    public Integer failCount;
}
