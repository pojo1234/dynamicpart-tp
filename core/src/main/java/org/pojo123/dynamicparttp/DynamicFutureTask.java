package org.pojo123.dynamicparttp;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author zhoudetao
 * @ClassName DynamicFutureTask
 * @description: TODO
 * @date 2025年06月18日
 * @version: 1.0
 */

public class DynamicFutureTask<V>  extends FutureTask<V> {

    /**
     * 用于记录当前任务
     */
   private final  Object originalTask;



    public DynamicFutureTask(Callable callable) {
        super(callable);
        originalTask=callable;
    }

    public  DynamicFutureTask(Runnable runnable){
        super(runnable,null);
        originalTask= runnable;
    }

    public  Object getAble(){
        return  originalTask;
    }
}
