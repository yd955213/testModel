package com.example.demo.configs;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: yd
 * @date: 2022-02-14
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "dev")
public class DevPropertiesConfig {
    private List<String> controlGroup;
    private List<String> recognitionGroup;
    private List<String> recognitionAndDownloadAuthorityGroup;
    private List<String> openAndCloseGroup;

}
