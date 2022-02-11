package com.example.demo.entity.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 上报身份数据处理结果 实体类
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class UploadAuthorityDealResult {
    public String Code;
    public String Msg;
    public String UniqueCode;
    public String CardNo;
    public String CardNo2;
    public String StartTime;
    public String IsLegal;
}
