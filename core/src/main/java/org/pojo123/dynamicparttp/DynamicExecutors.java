package org.pojo123.dynamicparttp;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
        return new DynamicPartThreadFactory(threadNamePrefix);
    }

    /**
     * 该拒绝策略慎用
     */
    static class DynamicAlertRejectPolicy implements RejectedExecutionHandler {

        private final AtomicLong rejectCount = new AtomicLong(0);
        //额定两次扩容
        private static final int rejectCountLimit = 1;
        private final AtomicBoolean isDumpStack = new AtomicBoolean(true);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            long incrementAndGet = rejectCount.incrementAndGet();
            int corePoolSize = executor.getCorePoolSize();
            int poolSize = executor.getPoolSize();
            int queueSize = executor.getQueue().size();
            int activeCount = executor.getActiveCount();
            String warnStr = String.format("当前线程池发生第%d次拒绝异常,activeCountSize:%d,coreSize:%d,poolSize:%d,queueSize:%d", incrementAndGet, activeCount, corePoolSize, poolSize, queueSize);
//            log.info(warnStr);
            System.out.println(warnStr);
            try {
//                QwAlertUtil.sendTextAlert(warnStr);
                if (isDumpStack.getAndSet(false) && executor instanceof DynamicPartThreadPool) {
                    //持久化
                    DynamicPartThreadPool dynamicPartThreadPool = (DynamicPartThreadPool) executor;
//                    FallbackThreadPool.backupPool.submit(() -> persistenceActiveThreads(dynamicPartThreadPool));
                    persistenceActiveThreads(dynamicPartThreadPool);
                }
            } catch (Exception e) {
//                log.error("DynamicPartPool告警转储失败!",e);
            }
            if (incrementAndGet <= rejectCountLimit) {
                System.out.println("线程池开始扩容"+executor);
                executor.setMaximumPoolSize((int) (executor.getMaximumPoolSize() * 1.5));
                executor.submit(r);
            } else {
                FallbackThreadPool.backupPool.submit(r);
            }
        }

        /**
         * 转储线程池stack
         */
        private void persistenceActiveThreads(DynamicPartThreadPool threadPool) {
            Set<Thread> activeThreads = threadPool.getActiveThreads();
            try {
                List<Map<String, Object>> threadDataList = new ArrayList<>();
                for (Thread thread : activeThreads) {
                    Map<String, Object> threadInfo = new LinkedHashMap<>();
                    threadInfo.put("threadName", thread.getName());
                    threadInfo.put("threadId", thread.getId());
                    threadInfo.put("state", thread.getState().toString());
                    threadInfo.put("priority", thread.getPriority());
                    threadInfo.put("isDaemon", thread.isDaemon());

                    // 添加堆栈轨迹
                    StackTraceElement[] stackTrace = thread.getStackTrace();
                    List<String> stackTraces = new ArrayList<>();
                    for (StackTraceElement ste : stackTrace) {
                        stackTraces.add(ste.toString());
                    }
                    threadInfo.put("stackTrace", stackTraces);
                    threadDataList.add(threadInfo);
                }
                String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
                Map<String, Object> logEntry = new LinkedHashMap<>();
                logEntry.put("timestamp", date);
                logEntry.put("threadCount", threadDataList.size());
                logEntry.put("threads", threadDataList);
                //这里获取任务具有一定的时延
                logEntry.put("executeTime",threadPool.getCurrentTaskInfo());
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String filePath = "DynamicPartThreadPool_dump_" + date + ".json";
                Files.write(new File(filePath).toPath(), objectMapper.writeValueAsString(logEntry).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
//              log.error("持久化线程dump文件失败",e);
                e.printStackTrace();
            }
        }


    }
}



