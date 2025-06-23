package org.pojo123.dynamicparttp.task;

/**
 * @author zhoudetao
 * @ClassName MonitoredRunnable
 * @description: 监控 Runable
 * @date 2025年06月22日
 * @version: 1.0
 */

public class TrackableRunnable<T extends Runnable> extends AbstractTrackableTask <T> implements Runnable {


    public TrackableRunnable(T runnable) {
            super(runnable);
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        threadName = Thread.currentThread().getName();
        task.run();
    }
}
