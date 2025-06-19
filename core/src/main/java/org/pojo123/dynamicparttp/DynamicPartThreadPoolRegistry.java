package org.pojo123.dynamicparttp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoudetao
 * @date 2025年06月17日 11:39
 * @Description 动态线程池集成器
 **/
public class DynamicPartThreadPoolRegistry {

    private ConcurrentHashMap<String, DynamicPartThreadPool> threadPoolExecutorMap = null;

    /**
     * 默认额定大小
     */
    private final int DEFAULT_RATED_SIZE=200;


    public DynamicPartThreadPoolRegistry() {
        threadPoolExecutorMap = new ConcurrentHashMap<>(15);
    }

    public DynamicPartThreadPoolRegistry(ConcurrentHashMap<String, DynamicPartThreadPool> theadPoolMap) {
        this.threadPoolExecutorMap = theadPoolMap;
    }

    public DynamicPartThreadPool getByThreadNamePrefix(String threadNamePrefix) {
        return threadPoolExecutorMap.get(threadNamePrefix);

    }


    public DynamicPartThreadPool createThreadPool(String namePrefix, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        if (threadPoolExecutorMap!=null && threadPoolExecutorMap.size()>DEFAULT_RATED_SIZE){
            throw new IllegalStateException("线程池数量超出限制: " + DEFAULT_RATED_SIZE);
        }
        DynamicPartThreadPool threadPoolExecutor = new DynamicPartThreadPool(namePrefix, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        threadPoolExecutorMap.putIfAbsent(namePrefix, threadPoolExecutor);
        return threadPoolExecutor;
    }
}
