package com.example.demo.entity.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class UpLoadRecords {

    public String DeviceUniqueCode;
    public Integer RecordID;
    public String RecordTime;
    public Integer ActionType;
    public Integer DeviceType;
    public Integer PersonType;
    public Integer InOutFlag;
    public Integer IsKqUse;
    public String UniqueCode;
    public String CapturePhoto;
    public String SimilarityScore;
    public String SimilarityThreshold;
    public String QualityScore;
    public String QualityThreshold;
    public Integer IsAlive;
    public Integer AccessDoorID;
    public String AccessCode;
    public String AccessResult;
    public Integer IDType;
    public String IDNo;
    public String CardNo;
    public Integer TemperatureDetectMode;
    public String TemperatureAlarmValue;
    public String TemperatureDetected;
}
