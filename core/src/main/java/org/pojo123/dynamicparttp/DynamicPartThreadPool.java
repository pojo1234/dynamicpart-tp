package org.pojo123.dynamicparttp;


import java.util.*;
import java.util.concurrent.*;

/**
 * @author zhoudetao
 * @date 2025年06月17日 10:12
 * @Description
 **/
public class DynamicPartThreadPool extends ThreadPoolExecutor {

    private final  ConcurrentHashMap<String,Long> taskExecuteTime=new ConcurrentHashMap<>();


    private Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());


    public DynamicPartThreadPool(String namePrefix, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,DynamicExecutors.defaultThreadFactory(namePrefix), new DynamicExecutors.DynamicAlertRejectPolicy());
    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        activeThreads.add(t);
        taskExecuteTime.put(t.getName(),System.currentTimeMillis());
        super.beforeExecute(t, r);
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        activeThreads.remove(Thread.currentThread());
        if ( r instanceof  DynamicFutureTask){
            DynamicFutureTask futureTask = (DynamicFutureTask) r;
            Object able = futureTask.getAble();
            if (able instanceof  DynamicPartTask){
                DynamicPartTask dynamicPartTask = (DynamicPartTask) able;
                taskExecuteTime.remove(dynamicPartTask.getThreadName());
            }
        }
        super.afterExecute(r, t);
    }

    /**
     * 获取当前存活的线程
     *
     * @return
     */
    public Set<Thread> getActiveThreads() {
        return activeThreads;
    }

    /**
     * 获取当前正在执行的任务时间
     *
     * @return
     */
    public Map<String,Long> getCurrentTaskInfo() {
        Map<String, Long> taskInfo = new HashMap<>();
        taskExecuteTime.forEach((k,v)->{
            taskInfo.put(k,System.currentTimeMillis()-v);
        });
        return taskInfo;
    }

    /**
     * 动态提交
     */
    public FutureTask submitDynamic(Runnable runnable){
        DynamicFutureTask futureTask = new DynamicFutureTask<>((Runnable) new DynamicPartTask(runnable));
        execute(futureTask);
        return futureTask;
    }



}
