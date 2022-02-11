package com.example.demo.entity.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 4.5下载人员身份数据 实体
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class DownloadAuthorityData {
    /**
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    public String StartTime;
    public String UniqueCode;
    /**
     * 人员类型
     * 0:一卡通系统员工
     * 1:访客
     * 2:陌生人
     * 99:其他
     */
    public Integer PersonType = 0;
    public String PersonNo;
    public String PersonName;
    /**
     * 性别
     * 0:保密
     * 1:男
     * 2:女
     */
    public Integer Gender =0;
    public String Photo;
    public String DptName = "测试部";
    public String IDType = "1";
    public String IDNo = null;
    /**
     * 身份合法性
     * Y:合法，需要设备保存身份数据
     * N:非法，需要设备删除人脸及身份数据
     */
    public String IsLegal = "Y";
    public String FaceStartUseTime;
    public String FaceStopUseTime;
    public String CardNo;
    public String CardStartUseTime;
    public String CardEndUseTime;
    public String CardNo2;
    public String CardStartUseTime2;
    public String CardEndUseTime2;
    public String CardTypeName = "1类卡组";
}
