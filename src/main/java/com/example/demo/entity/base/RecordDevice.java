package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-03-02
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class RecordDevice {
    String deviceUniqueCode;
    Integer recordCount = 0;
}
