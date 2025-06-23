package org.pojo123.dynamicparttp;

import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author zhoudetao
 * @date 2025年06月17日 14:23
 * @Description 线程执行器
 **/
public class DynamicExecutors {

    static class DynamicPartThreadFactory implements ThreadFactory {

        private static String NAME = "dynamicPartThreadPool";

        /**
         * 线程名称
         */
        private String threadName;

        /**
         * 线程数
         */
        private final AtomicInteger threadNum;

        /**
         * 线程组
         */
        private final ThreadGroup threadGroup;

        public DynamicPartThreadFactory(String threadNamePrefix) {
            this.threadName = NAME + "-" + threadNamePrefix;
            this.threadNum = new AtomicInteger(1);
            SecurityManager s = System.getSecurityManager();
            this.threadGroup = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            String threadName = this.threadName + "-" + threadNum.getAndIncrement();
            Thread thread = new Thread(threadGroup, r, threadName, 0);
            thread.setDaemon(false);
            thread.setUncaughtExceptionHandler((t, e) -> {
//                log.error(NAME + "catch exception:", e.getMessage());
                System.out.println("异常");
                e.printStackTrace();
            });
            return thread;
        }
    }

    public static ThreadFactory defaultThreadFactory(String threadNamePrefix) {
        if (StringUtils.isBlank(threadNamePrefix)){
            throw new IllegalArgumentException("线程名称前缀不可为空");
        }
        return new DynamicPartThreadFactory(threadNamePrefix);
    }



}



