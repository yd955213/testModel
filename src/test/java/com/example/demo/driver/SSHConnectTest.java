package com.example.demo.driver;

import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: yd
 * @date: 2022-02-15
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@SpringBootTest
class SSHConnectTest {
    @Autowired
    SSHConnect sshConnect;
    @Test
    @Disabled
    void createSshConnect() throws JSchException, IOException, ParseException {
//        测试用
//        sshConnect.createSshConnect("172.168.120.242");
//        sshConnect.executeCommand("ifconfig");

//        sshConnect.createSshConnect("172.168.120.130");
//        sshConnect.executeCommand("reboot");
//        sshConnect.disconnect();
        System.out.println(MediaType.APPLICATION_JSON_VALUE);
    }
}