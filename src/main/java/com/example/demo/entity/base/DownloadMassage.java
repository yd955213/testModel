package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: yd
 * @date: 2022-03-02
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
public class DownloadMassage {
    public String deviceUniqueCode;
    public List<Massage> massageList;

    @NoArgsConstructor
    @Data
    public static class Massage{
        public String uniqueCode;
        public String name;
        public String massage;
    }
}
