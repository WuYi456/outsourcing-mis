package com.tx.outsourcingmis.controller;

import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.common.ResultVO;
import com.tx.outsourcingmis.dto.JobApplicationRequest;
import com.tx.outsourcingmis.dto.JobApplicationResponse;
import com.tx.outsourcingmis.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 上岗申请控制器
 *
 * <p>提供外包人员上岗申请相关接口：
 * <ul>
 *   <li>提交上岗申请</li>
 *   <li>查询本人申请列表</li>
 *   <li>查看申请详情</li>
 *   <li>取消申请（仅待审批状态可取消）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/job-application")
@RequiredArgsConstructor
@Tag(name = "上岗申请模块", description = "测试外包人员上岗申请相关接口")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    /**
     * 提交上岗申请
     *
     * @param request 申请请求（含申请理由、备注）
     * @return 申请响应（含申请 ID、状态等）
     */
    @PostMapping("/submit")
    @Operation(summary = "提交上岗申请")
    @RequirePermission("job:submit")
    public ResultVO<JobApplicationResponse> submitApplication(@Valid @RequestBody JobApplicationRequest request) {
        return ResultVO.success(jobApplicationService.submitApplication(request));
    }

    /**
     * 获取当前用户的申请列表
     *
     * @return 申请列表
     */
    @GetMapping("/my-list")
    @Operation(summary = "获取我的申请列表")
    @RequirePermission("job:view")
    public ResultVO<List<JobApplicationResponse>> getMyApplications() {
        return ResultVO.success(jobApplicationService.getMyApplications());
    }

    /**
     * 根据 ID 获取申请详情
     *
     * @param id 申请 ID
     * @return 申请详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取申请详情")
    @RequirePermission("job:view")
    public ResultVO<JobApplicationResponse> getApplicationDetail(@PathVariable Long id) {
        return ResultVO.success(jobApplicationService.getApplicationDetail(id));
    }

    /**
     * 取消申请
     *
     * <p>仅当申请状态为「待审批」时可取消。
     *
     * @param id 申请 ID
     * @return 操作结果
     */
    @DeleteMapping("/cancel/{id}")
    @Operation(summary = "取消申请（仅待审批状态可取消）")
    @RequirePermission("job:cancel")
    public ResultVO<Void> cancelApplication(@PathVariable Long id) {
        jobApplicationService.cancelApplication(id);
        return ResultVO.success();
    }
}