package com.ycp.sample.service.impl;

import com.ycp.sample.api.SampleStreamingService;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author yingchengpeng
 * @date 2025/9/26 16:33
 */
@DubboService(protocol = "tri")
public class SampleStreamingServiceImpl implements SampleStreamingService {

    @Override
    public void longRunningOnlyInput(String input, StreamObserver<String> response) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            response.onError(e);
            return;
        }
        response.onNext(input);
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
                    Thread.sleep(3000L);
                    response.onNext(data);
                    if (isCompleted) {
                        response.onCompleted();
                    }
                } catch (InterruptedException e) {
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
