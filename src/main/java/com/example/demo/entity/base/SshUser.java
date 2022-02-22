package com.example.demo.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author: yd
 * @date: 2022-02-15
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
@Component
@Configuration
public class SshUser {
    @Value("${configs.ssh.userName}")
    public String userName;
    @Value("${configs.ssh.password}")
    public String password;
    @Value("${configs.ssh.port}")
    public Integer port;
    public String serverIP;

}
