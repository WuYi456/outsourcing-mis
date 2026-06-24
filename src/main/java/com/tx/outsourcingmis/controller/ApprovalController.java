package com.tx.outsourcingmis.controller;

import com.github.pagehelper.PageInfo;
import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.common.ResultVO;
import com.tx.outsourcingmis.dto.ApprovalRequest;
import com.tx.outsourcingmis.dto.ApprovalResponse;
import com.tx.outsourcingmis.mapper.JobApplicationMapper;
import com.tx.outsourcingmis.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
@Tag(name = "审批模块", description = "领导审批上岗申请")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final JobApplicationMapper jobApplicationMapper;

    @GetMapping("/pending")
    @Operation(summary = "分页查询待审批列表")
    @RequirePermission("approval:view")
    public ResultVO<PageInfo<ApprovalResponse>> getPendingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultVO.success(approvalService.getPendingList(pageNum, pageSize));
    }

    @PostMapping("/approve")
    @Operation(summary = "审批通过")
    @RequirePermission("approval:approve")
    public ResultVO<Void> approve(@Valid @RequestBody ApprovalRequest request) {
        approvalService.approve(request);
        return ResultVO.success();
    }

    @PostMapping("/batch-approve")
    @Operation(summary = "批量审批通过")
    @RequirePermission("approval:approve")
    public ResultVO<Void> batchApprove(@Valid @RequestBody ApprovalRequest request) {
        approvalService.batchApprove(request);
        return ResultVO.success();
    }

    @PostMapping("/reject")
    @Operation(summary = "审批拒绝")
    @RequirePermission("approval:reject")
    public ResultVO<Void> reject(@Valid @RequestBody ApprovalRequest request) {
        approvalService.reject(request);
        return ResultVO.success();
    }

    @PostMapping("/batch-reject")
    @Operation(summary = "批量审批拒绝")
    @RequirePermission("approval:reject")
    public ResultVO<Void> batchReject(@Valid @RequestBody ApprovalRequest request) {
        approvalService.batchReject(request);
        return ResultVO.success();
    }

    /**
     * 获取审批统计（待审批/已通过/已拒绝数量）
     * 用于仪表盘展示
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取审批统计")
    @RequirePermission("approval:view")
    public ResultVO<Map<String, Integer>> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", jobApplicationMapper.countByStatus(0));
        stats.put("approved", jobApplicationMapper.countByStatus(1));
        stats.put("rejected", jobApplicationMapper.countByStatus(2));

        log.info("获取审批统计 - 待审批: {}, 已通过: {}, 已拒绝: {}",
                stats.get("pending"), stats.get("approved"), stats.get("rejected"));

        return ResultVO.success(stats);
    }
}