package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-03-08
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class AllRebootMassage {
    public RebootMassage rebootMassage;
    public String deviceUniqueCode;
}
