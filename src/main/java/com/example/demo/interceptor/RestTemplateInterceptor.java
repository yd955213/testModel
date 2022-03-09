package com.example.demo.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author: yd
 * @date: 2022-03-08
 * @version: 1.0
 * @description:  拦截器http请求和响应, 写日志
 * @modifiedBy:
 */
@Controller
@Slf4j
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        requestLog(request, body);
        ClientHttpResponse clientHttpResponse = execution.execute(request, body);
        responseLog(request.getURI(), clientHttpResponse);
        return clientHttpResponse;
    }

    private void requestLog(HttpRequest request, byte[] body) throws IOException {
        log.debug("===========================request begin================================================");
        log.debug("URI         : {}", request.getURI());
        log.debug("Method      : {}", request.getMethod());
        log.debug("Headers     : {}", request.getHeaders());
        log.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
        log.debug("==========================request end================================================");
    }

    private void responseLog(URI uri, ClientHttpResponse clientHttpResponse) throws IOException {
        log.debug("============================response begin==========================================");
        log.debug("URI         : {}", uri);
        log.debug("Status code  : {}", clientHttpResponse.getStatusCode());
        log.debug("Status text  : {}", clientHttpResponse.getStatusText());
        log.debug("Headers      : {}", clientHttpResponse.getHeaders());
        log.debug("Response body: {}", StreamUtils.copyToString(clientHttpResponse.getBody(), Charset.defaultCharset()));
        log.debug("=======================response end=================================================");
    }
}
