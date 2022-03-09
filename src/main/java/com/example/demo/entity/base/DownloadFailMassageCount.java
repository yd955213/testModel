package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: yd
 * @date: 2022-03-01
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class DownloadFailMassageCount {
    public String deviceUniqueCode;
    public List<MassageCount> list;

    @NoArgsConstructor
    @Data
    public static class MassageCount{
        public String massage;
        public Integer count;
    }

}
