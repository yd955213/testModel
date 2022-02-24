package com.example.demo.SchedulingTask;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: yd
 * @date: 2022-02-15
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@SpringBootTest
class CommonTaskTest {

    @Autowired
    CommonTask commonTask;

    @Test
    void start(){
        commonTask.downLoadAuthority();
    }

    @Test
    void writeFile() throws FileNotFoundException {
//        String path1 = ClassUtils.getDefaultClassLoader().getResource("").getPath();
//
//        String path2 = ResourceUtils.getURL("classpath:").getPath();
//        System.out.println(path1);
//        System.out.println(path2);
//        System.out.println(System.getProperty("user.dir"));
//
//        System.out.println(new File("").getAbsolutePath());
        commonTask.writeFile();
    }

    @Test
    void reboot() {
        commonTask.reboot();
    }

    @Test
    void sendDingDingMassage() {
        commonTask.sendDingDingMassage();
    }
}