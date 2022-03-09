package com.example.demo.configs;

import com.example.demo.interceptor.RestTemplateInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yd
 * @date: 2022-02-11
 * @version: 1.0
 * @description: 向spring容器注册RestTemple bean 对象，@Autowired自动注入方式获取一个RestTemplate实例
 * @modifiedBy:
 */
@Configuration
public class RestTempleConfig {
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
        restTemplate.getInterceptors().add(new RestTemplateInterceptor());
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(10000);
        factory.setConnectTimeout(15000);
        return factory;
    }


    /**
     * 解决接口：明明返回是json数据，但是响应头里面指定的Content-Type 值却是 text/plain
     */
    public static class WxMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
        public WxMappingJackson2HttpMessageConverter(){
            List<MediaType> mediaTypes = new ArrayList<>();
            mediaTypes.add(MediaType.TEXT_PLAIN);
            mediaTypes.add(MediaType.TEXT_HTML);
            setSupportedMediaTypes(mediaTypes);
        }
    }
}
