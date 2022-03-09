package com.example.demo.entity.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yd
 * @date: 2022-03-04
 * @version: 1.0
 * @description: 3.4设备参数更新通知
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class NoticeOfDeviceParamsUpdate {
    public BasicParamsDTO basicParams;
    public RecognitionParamsDTO recognitionParams;
    public HardWareParamsDTO hardWareParams;

    @NoArgsConstructor
    @Data
    public static class BasicParamsDTO {
        public String DeviceName;
        public String ServerIP;
        public Integer ServerPort;
        public Integer IsAutoRestart;
        public String DailyRestartTime;
        public Integer QrCodeSwitch;
        public Integer IsSupportCard;
        public Integer MainUIType;
        public Integer HeartBeatInterval;
    }

    @NoArgsConstructor
    @Data
    public static class RecognitionParamsDTO {
        public String SimilityThreshold;
        public String QualityThreshold;
        public Integer MinFacePixel;
        public Integer MaxFacePixel;
        public Integer IsAlive;
        public String LivingThreshold;
    }

    @NoArgsConstructor
    @Data
    public static class HardWareParamsDTO {
        public Integer DebugModeSwitch;
    }
}
