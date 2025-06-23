package org.pojo123.dynamicparttp.threadpool;


import org.pojo123.dynamicparttp.DynamicExecutors;
import org.pojo123.dynamicparttp.task.TrackableFutureTask;
import org.pojo123.dynamicparttp.reject.FallBackAlertRejectPolicy;
import org.pojo123.dynamicparttp.task.AbstractMonitoredTask;
import org.pojo123.dynamicparttp.task.MonitoredCallable;
import org.pojo123.dynamicparttp.task.MonitoredRunnable;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author zhoudetao
 * @date 2025年06月17日 10:12
 * @Description
 **/
public class TraceableThreadPool  extends ThreadPoolExecutor {

    /**
     * 任务前缀
     */
    private  String namePrefix;

    /**
     * 线程执行时间
     */
    private final ConcurrentHashMap<String, Long> taskExecuteTime = new ConcurrentHashMap<>();


    /**
     * 当前活跃线程
     */
    private ConcurrentHashMap<String, Thread> activeThreads = new ConcurrentHashMap<>();


    public TraceableThreadPool(String namePrefix, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, DynamicExecutors.defaultThreadFactory(namePrefix), new FallBackAlertRejectPolicy());
        this.namePrefix=namePrefix;
    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (r instanceof TrackableFutureTask || r instanceof AbstractMonitoredTask) {
            activeThreads.put(Thread.currentThread().getName(), Thread.currentThread());
            taskExecuteTime.put(t.getName(), System.currentTimeMillis());
        }

        super.beforeExecute(t, r);
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (r instanceof TrackableFutureTask) {
            TrackableFutureTask futureTask = (TrackableFutureTask) r;
            Object originalTask = futureTask.getOriginalTask();
            if (originalTask instanceof AbstractMonitoredTask) {
               removeGrapher((AbstractMonitoredTask) originalTask);
            }
        }
        if (r instanceof AbstractMonitoredTask){
            removeGrapher((AbstractMonitoredTask) r);
        }

        super.afterExecute(r, t);
    }

    private void  removeGrapher(AbstractMonitoredTask monitoredTask ){
        taskExecuteTime.remove(monitoredTask.getThreadName());
        activeThreads.remove(monitoredTask.getThreadName());
    }

    /**
     * 获取当前存活的线程
     *
     * @return
     */
    public Set<Thread> getActiveThreads() {
        return new HashSet<>(activeThreads.values());
    }

    /**
     * 获取当前正在执行的任务时间
     *
     * @return
     */
    public Map<String, Long> getCurrentTaskInfo() {
        Map<String, Long> taskInfo = new HashMap<>();
        taskExecuteTime.forEach((k, v) -> {
            taskInfo.put(k, System.currentTimeMillis() - v);
        });
        return taskInfo;
    }

    /**
     * 动态提交
     */
    public Future<?> submitDynamic(Runnable runnable) {
        if (runnable == null) throw new NullPointerException();
        TrackableFutureTask<Void> futureTask = new TrackableFutureTask<>(new MonitoredRunnable<>(runnable));
        execute(futureTask);
        return futureTask;
    }

    /**
     * 动态提交
     */
    public <T> Future<T> submitDynamic(Runnable runnable, T result) {
        if (runnable == null) throw new NullPointerException();
        TrackableFutureTask<T> futureTask = new TrackableFutureTask<T>(new MonitoredRunnable<>(runnable), result);
        execute(futureTask);
        return futureTask;
    }

    /**
     * 动态提交
     */
    public <T> Future<T> submitDynamic(Callable<T> callable) {
        if (callable == null) throw new NullPointerException();
        TrackableFutureTask<T> futureTask = new TrackableFutureTask<>(new MonitoredCallable<>(callable));
        execute(futureTask);
        return futureTask;
    }


    /**
     * 动态提交
     *
     * @param runnable
     */
    public void executeDynamic(Runnable runnable) {
        if (runnable==null) throw  new NullPointerException();
        execute(new MonitoredRunnable<>(runnable));
    }

    /**
     * 获取线程池名称前缀
     * @return
     */
    public String getNamePrefix(){
        return namePrefix;
    }


}
