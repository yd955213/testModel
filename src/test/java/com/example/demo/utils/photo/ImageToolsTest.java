package com.example.demo.utils.photo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@SpringBootTest
class ImageToolsTest {

    @ParameterizedTest
    @ValueSource(strings = {"E:\\JavaWorkspace\\TestSoft\\demo3\\src\\main\\resources\\static\\facePhotos\\0109127.jpg","classpath:static/facePhotos/0109127.jpg", ""})
    void imageToBase64(String filePath) {
        ImageTools imageTools = new ImageTools();

        String s = imageTools.imageToBase64(filePath);
        File file = null;
        try {
            file = ResourceUtils.getFile(filePath);
            if (file.exists()) {
                Assertions.assertNotNull(s);
            } else {
                Assertions.assertNull(s);
            }
        } catch (FileNotFoundException e) {
            Assertions.assertNull(s);
        }

    }

    @Test
    void getFilePaths() throws FileNotFoundException {
        List<String> allFilePathsInDirectory = new ImageTools().getAllFilePathsInDirectory("classpath:static/facePhotos");

        System.out.println(allFilePathsInDirectory.size());
        System.out.println(new File(allFilePathsInDirectory.get(0)).getName());
    }
}