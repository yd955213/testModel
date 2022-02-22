package com.example.demo.entity.Wechat;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
@Repository
public class AccessToken {

    public String accessToken;
    public Integer expiresIn;
}
