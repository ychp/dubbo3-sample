package com.ycp.sample.api;

import org.apache.dubbo.common.stream.StreamObserver;

/**
 * @author yingchengpeng
 * @date 2025/9/26 16:20
 */
public interface SampleStreamingService {

    void longRunningOnlyInput(String input, StreamObserver<String> response);


    StreamObserver<String> longRunning(StreamObserver<String> response);
}
