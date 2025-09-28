package com.ycp.sample.dubbo.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.threadpool.manager.ExecutorRepository;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * @author yingchengpeng
 * @date 2025/9/28 10:51
 */
@Slf4j
//@Component
public class DubboThreadPoolMonitor {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::logActiveThreadCount, 0, 1, TimeUnit.SECONDS);
    }

    private void logActiveThreadCount() {
        try {
            // 获取Dubbo服务端线程池活跃线程数
            int activeThreads = getDubboActiveThreadCount();
            log.info("DubboExport活跃线程数: {}", activeThreads);
            // 获取Dubbo客户端端线程池活跃线程数
            activeThreads = getDubboReferActiveThreadCount();
            log.info("DubboRefer活跃线程数: {}", activeThreads);
        } catch (Exception e) {
            log.error("获取Dubbo线程池活跃线程数失败", e);
        }
    }

    private int getDubboActiveThreadCount() {
        // 通过SPI机制获取ExecutorRepository实例
        ExtensionLoader<ExecutorRepository> extensionLoader =
                ApplicationModel.defaultModel().getExtensionLoader(ExecutorRepository.class);
        ExecutorRepository executorRepository = extensionLoader.getDefaultExtension();

        ScheduledExecutorService executor = executorRepository.getServiceExportExecutor();

        // 如果是ScheduledThreadPoolExecutor，也可以获取活跃线程数
        if (executor instanceof java.util.concurrent.ScheduledThreadPoolExecutor) {
            return ((java.util.concurrent.ScheduledThreadPoolExecutor) executor).getActiveCount();
        }

        // 检查是否可以转换为ThreadPoolExecutor
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getActiveCount();
        }

        return 0;
    }

    private int getDubboReferActiveThreadCount() {
        // 通过SPI机制获取ExecutorRepository实例
        ExtensionLoader<ExecutorRepository> extensionLoader =
                ApplicationModel.defaultModel().getExtensionLoader(ExecutorRepository.class);
        ExecutorRepository executorRepository = extensionLoader.getDefaultExtension();

        ExecutorService executor = executorRepository.getServiceReferExecutor();
        // 获取活跃线程数
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getActiveCount();
        }
        return 0;
    }

    @PreDestroy
    public void stopMonitoring() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
