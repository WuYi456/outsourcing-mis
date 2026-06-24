package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.service.JvmMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.*;
import java.util.HashMap;
import java.util.Map;

/**
 * JVM 监控服务实现类
 *
 * <p>采集 JVM 运行指标并注册到 Prometheus：
 * <ul>
 *   <li>堆内存使用率</li>
 *   <li>非堆内存使用率</li>
 *   <li>系统平均负载</li>
 *   <li>可用处理器数</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JvmMetricsServiceImpl implements JvmMetricsService {

    private final MeterRegistry meterRegistry;

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

    /** 获取第一个 GC 收集器（通常为 G1 或 CMS） */
    private final GarbageCollectorMXBean gcMXBean = ManagementFactory.getGarbageCollectorMXBeans().stream()
            .findFirst()
            .orElse(null);

    @PostConstruct
    public void init() {
        registerCustomJvmMetrics();
    }

    /**
     * 注册自定义 JVM 指标到 Prometheus
     */
    private void registerCustomJvmMetrics() {
        meterRegistry.gauge("jvm.memory.heap.used.percent", this, JvmMetricsService::getHeapUsagePercent);
        meterRegistry.gauge("jvm.memory.nonheap.used.percent", this, JvmMetricsService::getNonHeapUsagePercent);
        meterRegistry.gauge("system.load.average", osMXBean, OperatingSystemMXBean::getSystemLoadAverage);
        meterRegistry.gauge("system.cpu.available", osMXBean, OperatingSystemMXBean::getAvailableProcessors);
        log.info("JVM 自定义指标已注册到 Prometheus");
    }

    @Override
    public double getHeapUsagePercent() {
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long max = heap.getMax();
        return max > 0 ? (double) heap.getUsed() / max * 100 : 0;
    }

    @Override
    public double getNonHeapUsagePercent() {
        MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
        long max = nonHeap.getMax();
        return max > 0 ? (double) nonHeap.getUsed() / max * 100 : 0;
    }

    @Override
    public long getGcCount() {
        return gcMXBean != null ? gcMXBean.getCollectionCount() : 0;
    }

    @Override
    public long getGcTotalTime() {
        return gcMXBean != null ? gcMXBean.getCollectionTime() : 0;
    }

    @Override
    public Map<String, Object> getJvmMemoryInfo() {
        Map<String, Object> info = new HashMap<>();

        // 堆内存信息
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        info.put("heap_used_bytes", heap.getUsed());
        info.put("heap_max_bytes", heap.getMax());
        info.put("heap_used_percent", getHeapUsagePercent());

        // 非堆内存信息
        MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
        info.put("nonheap_used_bytes", nonHeap.getUsed());
        info.put("nonheap_max_bytes", nonHeap.getMax());
        info.put("nonheap_used_percent", getNonHeapUsagePercent());

        // 线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        info.put("thread_count", threadMXBean.getThreadCount());
        info.put("peak_thread_count", threadMXBean.getPeakThreadCount());

        // GC 信息
        info.put("gc_count", getGcCount());
        info.put("gc_total_time_ms", getGcTotalTime());

        // 系统信息
        info.put("system_load_average", osMXBean.getSystemLoadAverage());
        info.put("available_processors", osMXBean.getAvailableProcessors());

        return info;
    }
}