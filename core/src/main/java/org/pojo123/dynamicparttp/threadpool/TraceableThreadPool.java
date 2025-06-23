package org.pojo123.dynamicparttp.threadpool;


import org.pojo123.dynamicparttp.DynamicExecutors;
import org.pojo123.dynamicparttp.task.AbstractTrackableTask;
import org.pojo123.dynamicparttp.task.TrackableCallable;
import org.pojo123.dynamicparttp.task.TrackableFutureTask;
import org.pojo123.dynamicparttp.reject.FallBackAlertRejectPolicy;
import org.pojo123.dynamicparttp.task.TrackableRunnable;


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
        if (r instanceof TrackableFutureTask || r instanceof AbstractTrackableTask) {
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
            if (originalTask instanceof AbstractTrackableTask) {
               removeGrapher((AbstractTrackableTask) originalTask);
            }
        }
        if (r instanceof AbstractTrackableTask){
            removeGrapher((AbstractTrackableTask) r);
        }

        super.afterExecute(r, t);
    }

    private void  removeGrapher(AbstractTrackableTask monitoredTask ){
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
    public Map<String, String> getCurrentTaskInfo() {
        Map<String, String> taskInfo = new HashMap<>();
        taskExecuteTime.forEach((k, v) -> {
            taskInfo.put(k, (System.currentTimeMillis() - v)+"ms");
        });
        return taskInfo;
    }

    /**
     * 动态提交
     */
    public Future<?> submitDynamic(Runnable runnable) {
        if (runnable == null) throw new NullPointerException();
        TrackableFutureTask<Void> futureTask = new TrackableFutureTask<>(new TrackableRunnable<>(runnable));
        execute(futureTask);
        return futureTask;
    }

    /**
     * 动态提交
     */
    public <T> Future<T> submitDynamic(Runnable runnable, T result) {
        if (runnable == null) throw new NullPointerException();
        TrackableFutureTask<T> futureTask = new TrackableFutureTask<T>(new TrackableRunnable<>(runnable), result);
        execute(futureTask);
        return futureTask;
    }

    /**
     * 动态提交
     */
    public <T> Future<T> submitDynamic(Callable<T> callable) {
        if (callable == null) throw new NullPointerException();
        TrackableFutureTask<T> futureTask = new TrackableFutureTask<>(new TrackableCallable(callable));
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
        execute(new TrackableRunnable<>(runnable));
    }

    /**
     * 获取线程池名称前缀
     * @return
     */
    public String getNamePrefix(){
        return namePrefix;
    }


}
