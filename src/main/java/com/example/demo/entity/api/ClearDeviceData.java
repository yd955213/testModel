package com.example.demo.entity.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author: yd
 * @date: 2022-02-18
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
@Repository
public class ClearDeviceData {
    public String clearPassRecord;
    public String clearPerson;
    public String clearLog;
}
