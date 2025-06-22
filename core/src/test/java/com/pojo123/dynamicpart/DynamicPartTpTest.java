package com.pojo123.dynamicpart;

import org.pojo123.dynamicparttp.DynamicPartThreadPoolManager;

import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author zhoudetao
 * @ClassName DynamicPartTpTest
 * @description: 动态线程池测试
 * @date 2025年06月22日
 * @version: 1.0
 */

public class DynamicPartTpTest {


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Future<Date> submit = Executors.newFixedThreadPool(1).submit(() -> new Date());
        DynamicPartThreadPoolManager.getInstance().getDynamicThreadPool("自定义业").submitDynamic(() -> {
            return new Date();
        });

        ArrayList<Future> futureTasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            futureTasks.add(DynamicPartThreadPoolManager.getInstance().getDynamicThreadPool("自定义业务" + i, 1, 2, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1)).submitDynamic(() -> {
                System.out.println(Thread.currentThread().getName() + ":业务" + finalI + "开始执行");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + ":业务" + finalI + "结束执行");
            }));
            if (finalI == 5) {
                //提交多个任务
                for (int j = 0; j < i; j++) {
                    try {
                        Thread.sleep(2000);
                        futureTasks.add(DynamicPartThreadPoolManager.getInstance().getDynamicThreadPool("自定义业务" + i).submitDynamic(() -> {
                            System.out.println(Thread.currentThread().getName() + ":业务5子任务开始执行");
                            try {
                                Thread.sleep(20000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println(Thread.currentThread().getName() + ":业务5子任务结束执行");
                        }));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

        }
        for (Future futureTask : futureTasks) {
            futureTask.get();
        }
        DynamicPartThreadPoolManager instance = DynamicPartThreadPoolManager.getInstance();
    }


}
