package com.ycp.sample;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yingchengpeng
 * @date 2025/9/26 16:19
 */
@SpringBootApplication
@EnableDubbo
public class SampleClient {
    public static void main(String[] args) {
        SpringApplication.run(SampleClient.class, args);
    }
}