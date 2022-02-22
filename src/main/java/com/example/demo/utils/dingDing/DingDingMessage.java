package com.example.demo.utils.dingDing;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: yd
 * @date: 2022-02-17
 * @version: 1.0
 * @description: 机器人发送消息支持的消息类型: text  官网地址:https://open.dingtalk.com/document/group/message-types-and-data-format
 * @modifiedBy:
 */
@NoArgsConstructor
@Data
@Repository
public class DingDingMessage {
    public AtDTO at;
    public TextDTO text;
    public String msgtype = "text";

    @NoArgsConstructor
    @Data
    public static class AtDTO {
        public List<String> atMobiles;
        public List<String> atUserIds;
        public Boolean isAtAll = false;
    }

    @NoArgsConstructor
    @Data
    public static class TextDTO {
        public String content;
    }

}
