package com.example.demo.conctroller;

import com.example.demo.entity.Wechat.AccessToken;
import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.restTemplateUtil.RestTemplateUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@Controller
//@RequestMapping("/cgi-bin")
@Log4j2
public class WechatNumberController {
    String hostIP = "https://sz.api.weixin.qq.com";
    @Autowired
    AccessToken accessToken;

    @Autowired
    RestTemplateUtil restTemplateUtil;

    public AccessToken  getAccessToken(){
//        String url = "https://sz.api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxd1b7519d80037a2a&secret=43b4b83682757fc72e28eed43934691b";
//        HttpHeaders headers = HttpHeadersUtil.getHeaders();
////        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
//        System.out.println(responseEntity);
//        AccessToken accessToken = JsonUtils.parseObject(responseEntity.getBody(), AccessToken.class);
//        System.out.println(accessToken);

        String url = hostIP + "/cgi-bin/token";
        // get 请求参数
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("grant_type", "client_credential");
        requestParams.add("appid", "wxd1b7519d80037a2a");
        requestParams.add("secret", "43b4b83682757fc72e28eed43934691b");
        String responseParams = restTemplateUtil.get(url, MediaType.APPLICATION_JSON, requestParams);
        if(!ObjectUtils.isEmpty(responseParams)){
            accessToken = JsonUtils.parseObject(responseParams, AccessToken.class);
        }
        log.info("AccessToken，json转换:{}", accessToken);
        /*
        {"access_token":"54_3ytTAKBbtN-RzJ_Ia21rtJeIS7muxj5q8J4bKJ249K06q5pUsUXQ2jtSMIiwIgEzMwCCMi69jcc6JShXDVQYxNNnSz-bVBFrGLDNmlHx-WkF5f7izirFG61MxDNccN4rF4r4e-tinM6HFGABMKEjAJAZXR","expires_in":7200}
         */
        return accessToken;
    }

}
