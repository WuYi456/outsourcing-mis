package com.tx.outsourcingmis.service;

import java.util.Map;

/**
 * JVM 监控服务接口
 *
 * <p>提供 JVM 内存、线程、GC 等指标采集，供 Prometheus 抓取。
 */
public interface JvmMetricsService {

    /**
     * 获取 JVM 内存详细信息
     *
     * @return JVM 指标 Map（堆/非堆内存、线程数、GC 信息等）
     */
    Map<String, Object> getJvmMemoryInfo();

    /**
     * 获取堆内存使用率（百分比）
     *
     * @return 堆内存使用率
     */
    double getHeapUsagePercent();

    /**
     * 获取非堆内存使用率（百分比）
     *
     * @return 非堆内存使用率
     */
    double getNonHeapUsagePercent();

    /**
     * 获取 GC 总次数
     *
     * @return GC 次数
     */
    long getGcCount();

    /**
     * 获取 GC 总耗时（毫秒）
     *
     * @return GC 总耗时
     */
    long getGcTotalTime();
}