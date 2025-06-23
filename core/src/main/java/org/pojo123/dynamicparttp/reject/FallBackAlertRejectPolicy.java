package org.pojo123.dynamicparttp.reject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.pojo123.dynamicparttp.degradation.FallBackExecutor;
import org.pojo123.dynamicparttp.threadpool.TraceableThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhoudetao
 * @ClassName DynamicAlertRejectPolicy
 * @description: 降级告警拒绝策略
 * @date 2025年06月22日
 * @version: 1.0
 */

public class FallBackAlertRejectPolicy implements RejectedExecutionHandler {
    private final static Logger logger = LoggerFactory.getLogger(FallBackAlertRejectPolicy.class);

    /**
     * 拒绝次数
     */
    private final AtomicLong rejectCount = new AtomicLong(0);
    private static final double EXPANSION_FACTOR = 1.5;
    private static final int REJECT_COUNT_LIMIT = 2;
    private static  final String UNKNOW_THREAD_EXECUTORS="UNKNOW_THREAD_EXECUTORS";

    private final AtomicBoolean isDumpStack = new AtomicBoolean(true);


    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        long incrementAndGet = rejectCount.incrementAndGet();
        int corePoolSize = executor.getCorePoolSize();
        int poolSize = executor.getPoolSize();
        int queueSize = executor.getQueue().size();
        int activeCount = executor.getActiveCount();
        String warnStr = String.format("当前线程池发生第%d次拒绝异常,activeCountSize:%d,coreSize:%d,poolSize:%d,queueSize:%d", incrementAndGet, activeCount, corePoolSize, poolSize, queueSize);
        logger.info(warnStr);
        TraceableThreadPool dynamicPartThreadPool = null;
        try {
//                QwAlertUtil.sendTextAlert(warnStr);
            if (isDumpStack.getAndSet(false) && executor instanceof TraceableThreadPool) {
                //持久化
                dynamicPartThreadPool  = (TraceableThreadPool) executor;
                persistenceActiveThreads(dynamicPartThreadPool);
            }
        } catch (Exception e) {
            logger.error("DynamicPartPool告警转储失败!", e);
        }
        if (incrementAndGet <= REJECT_COUNT_LIMIT) {
            int newMaxPoolSize=(int) (executor.getMaximumPoolSize() * 1.5);
            logger.info(String.format("线程池:%s开始扩容,oldMaxPoolSize:%d->newMaxPoolSize:%d",dynamicPartThreadPool==null?UNKNOW_THREAD_EXECUTORS:dynamicPartThreadPool.getNamePrefix(),executor.getMaximumPoolSize(),newMaxPoolSize));
            executor.setMaximumPoolSize(newMaxPoolSize);
            executor.submit(r);
        } else {
            FallBackExecutor.INSTANCE.submit(r);
        }
    }

    /**
     * 转储线程池stack
     */
    private void persistenceActiveThreads(TraceableThreadPool threadPool) {
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
            logEntry.put("executeTime", threadPool.getCurrentTaskInfo());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String filePath = "DynamicPartThreadPool_dump_" + date + ".json";
            Files.write(new File(filePath).toPath(), objectMapper.writeValueAsString(logEntry).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            logger.error("持久化线程dump文件失败", e);
            e.printStackTrace();
        }
    }

}
