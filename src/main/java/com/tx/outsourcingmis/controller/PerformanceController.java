package com.tx.outsourcingmis.controller;

import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.common.ResultVO;
import com.tx.outsourcingmis.dto.PerformanceEvaluateRequest;
import com.tx.outsourcingmis.dto.PerformanceResponse;
import com.tx.outsourcingmis.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 绩效管理控制器
 *
 * <p>提供外包人员绩效评定相关接口：
 * <ul>
 *   <li>评定绩效（使用 Redis 分布式锁防止并发冲突）</li>
 *   <li>确认绩效（二次确认机制）</li>
 *   <li>查询某用户的所有绩效</li>
 *   <li>查询某月所有人员的绩效（月度汇总）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Tag(name = "绩效管理模块", description = "外包人员绩效评定（使用Redis分布式锁）")
public class PerformanceController {

    private final PerformanceService performanceService;

    /**
     * 评定绩效
     *
     * <p>使用 Redis 分布式锁防止同一用户同一月份被并发评定。
     *
     * @param request 评定请求（含被评定用户、等级、年月）
     * @return 评定结果
     */
    @PostMapping("/evaluate")
    @Operation(summary = "评定绩效（使用分布式锁防并发）")
    @RequirePermission("performance:grade")
    public ResultVO<PerformanceResponse> evaluate(@Valid @RequestBody PerformanceEvaluateRequest request) {
        return ResultVO.success(performanceService.evaluate(request));
    }

    /**
     * 确认绩效
     *
     * <p>二次确认机制，只有评定人本人才能确认。
     *
     * @param performanceId 绩效 ID
     * @return 操作结果
     */
    @PutMapping("/confirm/{performanceId}")
    @Operation(summary = "确认绩效")
    @RequirePermission("performance:grade")
    public ResultVO<Void> confirmPerformance(@PathVariable Long performanceId) {
        performanceService.confirmPerformance(performanceId);
        return ResultVO.success();
    }

    /**
     * 获取某用户的所有绩效记录
     *
     * @param userId 用户 ID
     * @return 绩效列表（按年月倒序）
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取某用户的所有绩效")
    @RequirePermission("performance:view")
    public ResultVO<List<PerformanceResponse>> getUserPerformances(@PathVariable Long userId) {
        return ResultVO.success(performanceService.getUserPerformances(userId));
    }

    /**
     * 获取某月所有人员的绩效（月度汇总）
     *
     * @param year 年份
     * @param month 月份（1-12）
     * @return 该月所有人员的绩效列表
     */
    @GetMapping("/period")
    @Operation(summary = "获取某月所有人员的绩效")
    @RequirePermission("performance:view")
    public ResultVO<List<PerformanceResponse>> getPerformancesByPeriod(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return ResultVO.success(performanceService.getPerformancesByPeriod(year, month));
    }
}