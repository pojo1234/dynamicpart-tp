package org.pojo123.dynamicparttp.registry;


import org.pojo123.dynamicparttp.threadpool.TraceableThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoudetao
 * @date 2025年06月17日 11:39
 * @Description 动态线程池集成器
 **/
public class ThreadPoolRegistry {

    private ConcurrentHashMap<String, TraceableThreadPool> threadPoolExecutorMap = null;

    /**
     * 默认额定大小
     */
    private final int DEFAULT_RATED_SIZE=200;


    public ThreadPoolRegistry() {
        threadPoolExecutorMap = new ConcurrentHashMap<>(15);
    }

    public ThreadPoolRegistry(ConcurrentHashMap<String, TraceableThreadPool> theadPoolMap) {
        this.threadPoolExecutorMap = theadPoolMap;
    }

    public TraceableThreadPool getByThreadNamePrefix(String threadNamePrefix) {
        return threadPoolExecutorMap.get(threadNamePrefix);

    }


    /**
     * 创建线程池
     * @param namePrefix 线程名前缀
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime 空闲线程存活时间
     * @param unit 时间单位
     * @param workQueue 阻塞队列
     * @return
     */
    public TraceableThreadPool createThreadPool(String namePrefix, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        if (threadPoolExecutorMap!=null && threadPoolExecutorMap.size()>DEFAULT_RATED_SIZE){
            throw new IllegalStateException("线程池管理器线程池数量超出额定限制: " + DEFAULT_RATED_SIZE);
        }
        return  threadPoolExecutorMap.computeIfAbsent(namePrefix,k-> new TraceableThreadPool(namePrefix, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue));
    }

    /**
     * 关闭线程池
     */
    public void shutdownAll() {
        threadPoolExecutorMap.values().forEach(pool -> {
            try {
                //平滑关闭
                pool.shutdown();
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
        threadPoolExecutorMap.clear();
    }
}
