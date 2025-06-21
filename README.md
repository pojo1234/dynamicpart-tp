# dynamicpart-tp


## 📖 项目简介

DynamicPart-TP 是一个增强型的动态线程池框架，提供以下核心功能：
- **动态线程池管理**：支持运行时调整线程池参数
- **智能拒绝策略**：自动扩容机制 + 降级处理
- **线程活动监控**：实时跟踪线程状态和执行时间
- **故障转储**：自动持久化线程堆栈信息

## 🚀 核心特性

### 1. 动态线程池 (`DynamicPartThreadPool`)
继承自 `ThreadPoolExecutor` 的增强实现，内置线程活动追踪和任务执行时间统计

```java
public class DynamicPartThreadPool extends ThreadPoolExecutor {
    private final ConcurrentHashMap<String,Long> taskExecuteTime = new ConcurrentHashMap<>();
    private Set<Thread> activeThreads = Collections.synchronizedSet(new HashSet<>());
}
```

### 2.智能拒绝策略 (DynamicAlertRejectPolicy)

自动扩容机制：首次拒绝时扩容50%
降级处理：超过扩容阈值后转入备用线程池
自动生成线程转储文件（JSON格式）


### 3.集中式管理 (DynamicPartThreadPoolManager)

// 获取线程池单例
DynamicPartThreadPoolManager manager = DynamicPartThreadPoolManager.getInstance();
// 获取或创建线程池
DynamicPartThreadPool pool = manager.getDynamicThreadPool("payment-service");



## 🛠️ 使用示例


## 📊 监控能力

生成的转储文件示例：
```json
{
 "timestamp": "2025-06-21_142305",
 "threadCount": 3,
 "threads": [
  {
    "threadName": "dynamicPartThreadPool-order-process-1",
    "threadId": 42,
    "state": "RUNNABLE",
    "stackTrace": [
       "java.lang.Thread.sleep(Native Method)",
       "com.example.OrderProcessor.process(OrderProcessor.java:25)"
                 ]
  }
        ]
}
```

## 🔧 集成方式

Maven 依赖：
```xml
<dependency>
<groupId>org.pojo123</groupId>
<artifactId>dynamicpart-tp</artifactId>
<version>1.0.0</version>
</dependency>
```


## 📝 注意事项

默认线程池数量限制为200个（可通过修改DEFAULT_RATED_SIZE调整）

转储文件生成在项目根目录下

建议生产环境监控线程池状态

备用线程池使用AbortPolicy，请注意任务丢失风险

动态扩容功能慎用于稳定性要求高的场景
