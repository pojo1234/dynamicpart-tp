package org.pojo123.dynamicparttp.task;

import java.util.concurrent.Callable;

/**
 * @author zhoudetao
 * @ClassName MonitoredCallable
 * @description: 监控Callable
 * @date 2025年06月22日
 * @version: 1.0
 */

public class  TrackableCallable<V> extends AbstractTrackableTask<Callable<V>> implements Callable<V> {


    public TrackableCallable(Callable<V> task) {
        super(task);
    }

    @Override
    public V call() throws Exception {
        startTime=System.currentTimeMillis();
        threadName=Thread.currentThread().getName();
        return task.call();
    }
}
