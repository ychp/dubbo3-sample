package com.ycp.sample.service.impl;

import com.ycp.sample.api.SampleService;
import com.ycp.sample.component.LongMethodComponent;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author yingchengpeng
 * @date 2025/9/26 16:31
 */
@DubboService
public class SampleServiceImpl implements SampleService {
    @Resource
    private LongMethodComponent longMethodComponent;

    @Override
    public String longRunning(String input) {
        return longMethodComponent.longMethod(input);
    }
}
