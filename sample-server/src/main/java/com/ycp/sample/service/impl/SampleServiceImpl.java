package com.ycp.sample.service.impl;

import com.ycp.sample.api.SampleService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author yingchengpeng
 * @date 2025/9/26 16:31
 */
@DubboService
public class SampleServiceImpl implements SampleService {
    @Override
    public String longRunning(String input) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return input;
    }
}
