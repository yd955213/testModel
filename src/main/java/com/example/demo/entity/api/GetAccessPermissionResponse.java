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
public class GetAccessPermissionResponse {
    /**
     * 权限结果
     * Y:允许通行
     * N:禁止通行
     */
    public String Permission;
    /**
     * 权限结果说明
     */
    public String Description;
}
