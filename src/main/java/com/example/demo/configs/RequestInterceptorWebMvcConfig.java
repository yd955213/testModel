package com.example.demo.configs;

import com.example.demo.interceptor.DispatcherServlet;
import com.example.demo.interceptor.RequestInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author: yd
 * @date: 2022-02-10
 * @version: 1.0
 * @description: WebMVC配置，你可以集中在这里配置拦截器、过滤器、静态资源缓存等
 * @modifiedBy:
 */
@Configuration
public class RequestInterceptorWebMvcConfig implements WebMvcConfigurer {
  @Resource
  private RequestInterceptor requestInterceptor;
  @Override
  /*
   * 配置拦截器
   */
  public void addInterceptors(InterceptorRegistry registry) {
    // 添加需要拦截的 uri
    registry.addInterceptor(requestInterceptor).addPathPatterns("/**")
    .excludePathPatterns("/test/**");
  }
  @Bean
  @Qualifier(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
  public org.springframework.web.servlet.DispatcherServlet dispatcherServlet() {
    return new DispatcherServlet();
  }
}