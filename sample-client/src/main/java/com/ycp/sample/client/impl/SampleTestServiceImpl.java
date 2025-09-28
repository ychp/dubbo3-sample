package com.ycp.sample.client.impl;

import com.ycp.sample.api.SampleService;
import com.ycp.sample.api.SampleStreamingService;
import com.ycp.sample.api.SampleTestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.rpc.RpcContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author yingchengpeng
 * @date 2025/9/26 17:23
 */
@Slf4j
@DubboService(protocol = "tri", timeout = 60000)
public class SampleTestServiceImpl implements SampleTestService {

    @DubboReference(timeout = 10000)
    private SampleService sampleService;

    @DubboReference(protocol = "tri", timeout = 1000)
    private SampleStreamingService sampleStreamingService;

    @Override
    public void test() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.info("开始测试");
        String result;
        result = sampleService.longRunning("hello world");
        log.info("常规调用，耗时 {} ms, 结果：{}", System.currentTimeMillis() - startTime, result);

        startTime = System.currentTimeMillis();
        result = getCompleteContentByResponse();
        log.info("只有response的流式通信，耗时 {} ms, 结果：{}", System.currentTimeMillis() - startTime, result);

        startTime = System.currentTimeMillis();
        result = getCompleteContentByResponseSync();
        log.info("只有response的流式通信-同步，耗时 {} ms, 结果：{}", System.currentTimeMillis() - startTime, result);

        startTime = System.currentTimeMillis();
        CompletableFuture<String> resultFuture = getCompleteContent();
        if (resultFuture.isCompletedExceptionally()) {
            log.info("标准流式通信, 失败，耗时 {} ms, 结果：{}", System.currentTimeMillis() - startTime, resultFuture.exceptionNow());
        } else {
            log.info("标准流式通信，耗时 {} ms, 结果：{}", System.currentTimeMillis() - startTime, resultFuture.get());
        }

        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            startTime = System.currentTimeMillis();
            futures.add(getCompleteContent());
            log.info("标准流式通信, 模拟服务端线程池满，连续调用 {}，耗时 {} ms", i, System.currentTimeMillis() - startTime);
        }

        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<String> future = futures.get(i);
            if (future.isCompletedExceptionally()) {
                log.info("标准流式通信, 模拟服务端线程池满，连续调用 {} 失败，耗时 {} ms, 结果：{}", i, System.currentTimeMillis() - startTime, future.exceptionNow());
                continue;
            }
            log.info("标准流式通信, 模拟服务端线程池满，连续调用 {}，耗时 {} ms, 结果：{}", i, System.currentTimeMillis() - startTime, future.get());
        }

    }


    /**
     * 对外提供的接口方法，返回完整的content
     */
    public String getCompleteContentByResponse() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.info("只有response的流式通信, 开始处理");

        // 使用CompletableFuture包装异步结果
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder contentBuilder = new StringBuilder();

        // 创建响应观察者
        StreamObserver<String> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(String result) {
                // 累积文本片段
                contentBuilder.append(result);
            }

            @Override
            public void onCompleted() {
                // 完成时将结果传入Future
                String content = contentBuilder.toString();
                future.complete(content); // 标记Future完成并设置结果
            }

            @Override
            public void onError(Throwable throwable) {
                // 错误时将异常传入Future
                future.completeExceptionally(throwable);
            }
        };

        // 发送数据
        sampleStreamingService.longRunningOnlyInput("hello world", responseObserver);
        CompletableFuture<Void> responseFuture =  RpcContext.getServiceContext().getCompletableFuture();
        if (responseFuture.isCompletedExceptionally()) {
            log.info("只有response的流式通信, 接口调用失败，耗时 {} ms, 结果：{}", System.currentTimeMillis() - startTime, future.exceptionNow());
            throw new RuntimeException(responseFuture.exceptionNow());
        }
        log.info("只有response的流式通信, 接口调用完成, 耗时 {} ms", System.currentTimeMillis() - startTime);


        // 阻塞等待结果完成，然后返回（会阻塞当前线程）
        return future.get();
    }
    /**
     * 对外提供的接口方法，返回完整的content
     */
    public String getCompleteContentByResponseSync() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.info("只有response的流式通信-同步, 开始处理");

        // 使用CompletableFuture包装异步结果
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder contentBuilder = new StringBuilder();

        // 创建响应观察者
        StreamObserver<String> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(String result) {
                // 累积文本片段
                contentBuilder.append(result);
            }

            @Override
            public void onCompleted() {
                // 完成时将结果传入Future
                String content = contentBuilder.toString();
                future.complete(content); // 标记Future完成并设置结果
            }

            @Override
            public void onError(Throwable throwable) {
                // 错误时将异常传入Future
                future.completeExceptionally(throwable);
            }
        };

        // 发送数据
        sampleStreamingService.longRunningOnlyInputSync("hello world", responseObserver);
        log.info("只有response的流式通信-同步, 接口调用完成, 耗时 {} ms", System.currentTimeMillis() - startTime);

        // 阻塞等待结果完成，然后返回（会阻塞当前线程）
        return future.get();
    }

    /**
     * 对外提供的接口方法，返回完整的content
     */
    public CompletableFuture<String> getCompleteContent() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.info("标准流式通信, 开始处理");

        // 使用CompletableFuture包装异步结果
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder contentBuilder = new StringBuilder();

        // 创建响应观察者
        StreamObserver<String> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(String result) {
                // 累积文本片段
                contentBuilder.append(result);
            }

            @Override
            public void onCompleted() {
                // 完成时将结果传入Future
                String content = contentBuilder.toString();
                future.complete(content); // 标记Future完成并设置结果
            }

            @Override
            public void onError(Throwable throwable) {
                // 错误时将异常传入Future
                future.completeExceptionally(throwable);
            }
        };

        // 发送数据
        StreamObserver<String> request = sampleStreamingService.longRunning(responseObserver);
        log.info("标准流式通信, 接口调用完成, 耗时 {} ms", System.currentTimeMillis() - startTime);

        try {
            // 发送测试数据
            request.onNext("hello world");
            request.onCompleted();
        } catch (Exception e) {
            request.onError(e);
            future.completeExceptionally(e);
        }

        // 阻塞等待结果完成，然后返回（会阻塞当前线程）
        return future;
    }
}
