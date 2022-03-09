package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yd
 * @date: 2022-03-08
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class RebootMassage {
    public String deviceUniqueCode;
    public Integer rebootCount = 0;
    public List<String> rebootTimeList = new ArrayList<>();
}
