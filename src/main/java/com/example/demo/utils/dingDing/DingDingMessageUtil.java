package com.example.demo.utils.dingDing;

import com.example.demo.utils.JsonUtils;
import com.example.demo.utils.restTemplateUtil.RestTemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description:
 * @modifiedBy:
 */
@Component
public class DingDingMessageUtil {

    private final static String url = "https://oapi.dingtalk.com/robot/send?access_token=c2e5b3863dbdaf6e406af8694b4773e8133d0bd980b5684fd86d0dd0c643cbe2";

    @Autowired
    RestTemplateUtil restTemplateUtil;
    @Autowired
    DingDingMessage dingDingMessage;

    public void sendMassage(String massage){
        long timestamp = System.currentTimeMillis();
        String sign = new Sign().encode(timestamp);

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("timestamp", Long.toString(timestamp));
        multiValueMap.add("sign", sign);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.queryParams(multiValueMap).build().encode().toUri();
        System.out.println(uri);
        System.out.println(dingDingMessage);
        // 内部类没有 自动注入未成功，为啥呢？
//        dingDingMessage.getText().setContent(massage);
        DingDingMessage.TextDTO textDTO = new DingDingMessage.TextDTO();
        textDTO.setContent(massage);

        dingDingMessage.setText(textDTO);
        System.out.println(dingDingMessage);
        String requestParams = JsonUtils.toJsonStringNotNull(dingDingMessage, DingDingMessage.class);
        String responseParams = restTemplateUtil.post(uri, requestParams);
        System.out.println(responseParams);

    }
}
