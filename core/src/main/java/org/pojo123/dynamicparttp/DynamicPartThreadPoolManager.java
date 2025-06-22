package org.pojo123.dynamicparttp;


import org.pojo123.dynamicparttp.registry.ThreadPoolRegistry;
import org.pojo123.dynamicparttp.threadpool.TraceableThreadPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoudetao
 * @date 2025年06月17日 09:58
 * @Description 动态分区线程池工厂
 * 由于原ThreadPoolUtil存在大量业务逻辑调用，一个业务逻辑导致线程池出现问题，会导致所有相关业务发生雪崩影响
 * 所以这里进行拆解分离，保证局部可用性
 * <p>
 * 需要保证执行其方法时拿到的是一个代理类
 * @see
 **/
public class DynamicPartThreadPoolManager {

    private final ThreadPoolRegistry threadPoolRegistry;

    private static volatile DynamicPartThreadPoolManager dynamicPartThreadPoolManager;


    public DynamicPartThreadPoolManager() {
        this.threadPoolRegistry = new ThreadPoolRegistry();
    }


    public static DynamicPartThreadPoolManager getInstance() {
        //double check
        if (dynamicPartThreadPoolManager == null) {
            synchronized (DynamicPartThreadPoolManager.class) {
                if (dynamicPartThreadPoolManager == null) {
                    dynamicPartThreadPoolManager = new DynamicPartThreadPoolManager();
                }
            }
        }
        return dynamicPartThreadPoolManager;
    }

    /**
     * 获取动态分区线程池
     *
     * @return ThreadPoolExecutor 线程池
     */
    public TraceableThreadPool getDynamicThreadPool(String threadNamePrefix) {
        return getDynamicThreadPool(threadNamePrefix, 2, 8, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
    }

    /**
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   空闲线程最大存活时间
     * @param unit            时间单位
     * @param workQueue       工作队列
     * @return
     */
    public TraceableThreadPool getDynamicThreadPool(String namePrefix, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        //先根据要求查询 theadPoolMap中是否含有要求的线程池，没有则新增，有则直接拿出来
        TraceableThreadPool threadPool = threadPoolRegistry.getByThreadNamePrefix(namePrefix);
        if (threadPool == null) {
            return threadPoolRegistry.createThreadPool(namePrefix, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }
        return threadPool;
    }


}
