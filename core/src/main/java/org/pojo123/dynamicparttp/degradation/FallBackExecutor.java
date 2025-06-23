package org.pojo123.dynamicparttp.degradation;

import org.pojo123.dynamicparttp.threadpool.TraceableThreadPool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoudetao
 * @date 2025年06月17日 15:23
 * @Description 当主线程池拒绝任务时，使用此备用执行器处理任务（降级策略）
 **/
public class FallBackExecutor {
    public static final ThreadPoolExecutor INSTANCE = new TraceableThreadPool("DoctorExecutor",
            5,  // 核心线程数
            20,  // 最大线程数
            1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000)
    );

    // 禁止实例化
    private FallBackExecutor() {
    }
}
