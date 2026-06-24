package com.tx.outsourcingmis.controller;

import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.common.ResultVO;
import com.tx.outsourcingmis.service.JvmMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * JVM 监控控制器
 *
 * <p>提供 JVM 运行状态监控接口：
 * <ul>
 *   <li>JVM 基本信息（版本、厂商、启动时间、运行时长）</li>
 *   <li>JVM 内存使用详情（堆/非堆内存、GC 信息）</li>
 *   <li>线程堆栈快照（用于排查死锁和性能问题）</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Tag(name = "JVM监控模块", description = "JVM内存、线程、GC监控")
public class JvmMonitorController {

    private final JvmMetricsService jvmMetricsService;

    /** 线程堆栈最大显示数量，防止响应过大 */
    private static final int MAX_THREAD_DISPLAY = 50;

    /**
     * 获取 JVM 详细信息
     *
     * @return JVM 版本、厂商、启动时间、运行时长、内存信息
     */
    @GetMapping("/jvm/info")
    @Operation(summary = "获取JVM详细信息")
    @RequirePermission("monitor:jvm")
    public ResultVO<Map<String, Object>> getJvmInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("jvm_name", System.getProperty("java.vm.name"));
        info.put("jvm_version", System.getProperty("java.version"));
        info.put("jvm_vendor", System.getProperty("java.vendor"));
        info.put("start_time", ManagementFactory.getRuntimeMXBean().getStartTime());
        info.put("uptime_ms", ManagementFactory.getRuntimeMXBean().getUptime());
        info.put("memory", jvmMetricsService.getJvmMemoryInfo());
        return ResultVO.success(info);
    }

    /**
     * 获取 JVM 内存使用详情
     *
     * @return 堆内存、非堆内存、线程数、GC 信息
     */
    @GetMapping("/jvm/memory")
    @Operation(summary = "获取JVM内存使用详情")
    @RequirePermission("monitor:jvm")
    public ResultVO<Map<String, Object>> getJvmMemory() {
        return ResultVO.success(jvmMetricsService.getJvmMemoryInfo());
    }

    /**
     * 获取线程堆栈快照
     *
     * <p>返回当前所有线程的摘要信息，并检测是否存在死锁。
     * <p>线程数量限制为 {@link #MAX_THREAD_DISPLAY} 条，避免响应过大。
     *
     * @return 线程统计信息、线程摘要列表、死锁检测结果
     */
    @GetMapping("/thread/dump")
    @Operation(summary = "获取线程堆栈（用于排查死锁和性能问题）")
    @RequirePermission("monitor:jvm")
    public ResultVO<Map<String, Object>> getThreadDump() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> result = new HashMap<>();

        // 线程统计
        result.put("live_thread_count", threadMXBean.getThreadCount());
        result.put("peak_thread_count", threadMXBean.getPeakThreadCount());
        result.put("daemon_thread_count", threadMXBean.getDaemonThreadCount());

        // 线程摘要（限制数量防止响应过大）
        Map<Long, String> summaries = new HashMap<>();
        int count = 0;
        for (long threadId : threadMXBean.getAllThreadIds()) {
            if (count++ >= MAX_THREAD_DISPLAY) {
                break;
            }
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
            if (threadInfo != null) {
                summaries.put(threadId, threadInfo.getThreadName() + " - " + threadInfo.getThreadState());
            }
        }
        result.put("thread_summaries", summaries);

        // 死锁检测
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            result.put("deadlock_detected", true);
            result.put("deadlock_thread_ids", deadlockedThreads);
            log.warn("检测到死锁，涉及线程: {}", deadlockedThreads);
        } else {
            result.put("deadlock_detected", false);
        }

        return ResultVO.success(result);
    }
}