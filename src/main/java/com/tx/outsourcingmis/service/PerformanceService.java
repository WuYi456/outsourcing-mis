package com.tx.outsourcingmis.service;

import com.tx.outsourcingmis.dto.PerformanceEvaluateRequest;
import com.tx.outsourcingmis.dto.PerformanceResponse;

import java.util.List;

/**
 * 绩效管理服务接口
 *
 * <p>提供外包人员绩效评定相关业务操作。
 */
public interface PerformanceService {

    /**
     * 评定绩效（使用 Redis 分布式锁防止并发冲突）
     *
     * @param request 评定请求
     * @return 评定结果
     */
    PerformanceResponse evaluate(PerformanceEvaluateRequest request);

    /**
     * 确认绩效（二次确认机制）
     *
     * @param performanceId 绩效 ID
     */
    void confirmPerformance(Long performanceId);

    /**
     * 获取某用户的所有绩效记录
     *
     * @param userId 用户 ID
     * @return 绩效列表
     */
    List<PerformanceResponse> getUserPerformances(Long userId);

    /**
     * 获取某月所有人员的绩效（月度汇总）
     *
     * @param year 年份
     * @param month 月份
     * @return 绩效列表
     */
    List<PerformanceResponse> getPerformancesByPeriod(Integer year, Integer month);
}