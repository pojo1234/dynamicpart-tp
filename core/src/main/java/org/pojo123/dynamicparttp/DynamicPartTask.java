package org.pojo123.dynamicparttp;

import java.util.concurrent.Callable;

/**
 * @author zhoudetao
 * @date 2025年06月18日 11:45
 * @Description Wrapper Runnable or Callable
 **/
public class DynamicPartTask implements Runnable, Callable {

    private long startTime;

    private String threadName;

    private Runnable runnable;

    private Callable callable;

    public DynamicPartTask(Runnable runnable) {
        this.runnable = runnable;
    }

    public DynamicPartTask(Callable callable) {
        this.callable = callable;
    }

    @Override
    public void run() {
        startTime=System.currentTimeMillis();
        threadName=Thread.currentThread().getName();
        runnable.run();
    }

    @Override
    public Object call() throws Exception {
        startTime=System.currentTimeMillis();
        threadName=Thread.currentThread().getName();
        return callable.call();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }


}
