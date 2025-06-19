package org.pojo123.dynamicparttp;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoudetao
 * @date 2025年06月17日 15:23
 * @Description
 **/
public class FallbackThreadPool {
    public static final ThreadPoolExecutor backupPool = new ThreadPoolExecutor(
            5,  // 核心线程数
            20,  // 最大线程数
            1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            DynamicExecutors.defaultThreadFactory("DynamicFallbackThread"),
            new ThreadPoolExecutor.AbortPolicy()
    );
}
