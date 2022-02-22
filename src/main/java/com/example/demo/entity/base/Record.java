package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-02-21
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class Record {
    String uniqueCode;
    String name;
    Integer count;
}
