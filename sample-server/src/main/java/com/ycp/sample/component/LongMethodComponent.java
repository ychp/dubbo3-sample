package com.ycp.sample.component;

import org.springframework.stereotype.Component;

/**
 * @author yingchengpeng
 * @date 2025/9/28 10:07
 */
@Component
public class LongMethodComponent {

    public String longMethod(String input) {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return input;
    }
}
