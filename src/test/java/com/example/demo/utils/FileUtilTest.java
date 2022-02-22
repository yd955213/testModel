package com.example.demo.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;


/**
 * @author: yd
 * @date: 2022-02-21
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
class FileUtilTest {

    @Test
    @Disabled
    void toZip() {
        String fileDir = "C:\\Users\\yangdang\\Desktop\\新建文件夹";
        String zipPath = "C:\\Users\\yangdang\\Desktop\\新建文件夹\\test.zip";

        List<File> files = Arrays.asList(new File(fileDir).listFiles());
        System.out.println(files);
        new FileUtil().toZip(files, zipPath);
    }
}