package com.example.demo.service.init;

import com.example.demo.entity.api.DownloadAuthorityData;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: yd
 * @date: 2022-02-15
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@Data
@NoArgsConstructor
public class TestInfo {
    private final static Map<String, DownloadAuthorityData> testInfo = Collections.synchronizedMap(new HashMap<>());
}
