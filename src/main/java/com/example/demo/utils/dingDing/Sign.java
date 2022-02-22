package com.example.demo.utils.dingDing;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description: 通过钉钉的自定义机器人，可以将消息同步到钉钉的聊天群 https://open.dingtalk.com/document/robots/customize-robot-security-settings
 * @modifiedBy:
 */
public class Sign {
    private final static String secret  = "SECa1498dea225517d8336513ba2be1c1e65cb8576232f06ae33c386cf4e66a7625";

    public String encode(Long timestamp){
//        timestamp = System.currentTimeMillis();
        String secretKey = timestamp + "\n" + secret;
        Mac mac;
        String sign = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(secretKey.getBytes(StandardCharsets.UTF_8));
            sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8);
            System.out.println(sign);
            System.out.println(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

}
