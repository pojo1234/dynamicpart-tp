package org.pojo123.dynamicparttp.task;

/**
 * @author zhoudetao
 * @ClassName AbstractMonitoredTask
 * @description: TODO
 * @date 2025年06月22日
 * @version: 1.0
 */

public abstract class AbstractTrackableTask<T> {

    /**
     * 任务执行开始时间
     */
    protected long startTime;

    /**
     * 执行线程名
     */
    protected String threadName;


    protected T task;

    public AbstractTrackableTask(T task) {
        this.task = task;
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
