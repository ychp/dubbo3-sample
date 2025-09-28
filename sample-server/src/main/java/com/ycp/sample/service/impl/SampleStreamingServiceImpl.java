package com.ycp.sample.service.impl;

import com.ycp.sample.api.SampleStreamingService;
import com.ycp.sample.component.LongMethodComponent;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author yingchengpeng
 * @date 2025/9/26 16:33
 */
@DubboService(protocol = "tri")
public class SampleStreamingServiceImpl implements SampleStreamingService {
    @Resource
    private LongMethodComponent longMethodComponent;

    @Override
    public void longRunningOnlyInput(String input, StreamObserver<String> response) {
        try {
            response.onNext(longMethodComponent.longMethod(input));
        } catch (Exception e) {
            response.onError(e);
            return;
        }
        response.onCompleted();
    }

    @Override
    public StreamObserver<String> longRunning(StreamObserver<String> response) {
        return new StreamObserver<>() {
            boolean isCompleted = false;
            boolean isSleep = false;

            @Override
            public void onNext(String data) {
                try {
                    isSleep = true;
                    response.onNext(longMethodComponent.longMethod(data));
                    if (isCompleted) {
                        response.onCompleted();
                    }
                } catch (Exception e) {
                    response.onError(e);
                } finally {
                    isSleep = false;
                }

            }

            @Override
            public void onError(Throwable throwable) {
                response.onError(throwable);
            }

            @Override
            public void onCompleted() {
                if (isSleep) {
                    isCompleted = true;
                    return;
                }
                response.onCompleted();
            }
        };
    }
}
