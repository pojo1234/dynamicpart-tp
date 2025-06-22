package org.pojo123.dynamicparttp.task;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author zhoudetao
 * @ClassName DynamicFutureTask
 * @description: 动态线程任务
 * @date 2025年06月18日
 * @version: 1.0
 */

public class TrackableFutureTask<V>  extends FutureTask<V> {

    /**
     * 用于记录当前任务
     */
   private final  Object originalTask;



    public TrackableFutureTask(Callable callable) {
        super(callable);
        originalTask=callable;
    }

    public  TrackableFutureTask(Runnable runnable){
        super(runnable,null);
        originalTask= runnable;
    }
    public  TrackableFutureTask(Runnable runnable,V result){
        super(runnable,result);
        originalTask= runnable;
    }

    public  Object getOriginalTask(){
        return  originalTask;
    }
}
