package com.tx.outsourcingmis.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tx.outsourcingmis.dto.ApprovalNotificationMessage;
import com.tx.outsourcingmis.dto.ApprovalRequest;
import com.tx.outsourcingmis.dto.ApprovalResponse;
import com.tx.outsourcingmis.entity.JobApplication;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.JobApplicationMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.ApprovalService;
import com.tx.outsourcingmis.service.NotificationService;
import com.tx.outsourcingmis.utils.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批服务实现类
 *
 * <p>核心功能：
 * <ul>
 *   <li>查询待审批列表（支持分页）</li>
 *   <li>单个/批量审批通过</li>
 *   <li>单个/批量审批拒绝（需填写拒绝原因）</li>
 * </ul>
 * <p>审批完成后通过 MQ 异步通知相关方。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final JobApplicationMapper jobApplicationMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    public PageInfo<ApprovalResponse> getPendingList(Integer pageNum, Integer pageSize) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        PageHelper.startPage(pageNum, pageSize);
        List<JobApplication> applications = jobApplicationMapper.selectPendingByApproverId(userId);

        PageInfo<JobApplication> pageInfo = new PageInfo<>(applications);
        List<ApprovalResponse> responses = applications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageInfo<ApprovalResponse> result = new PageInfo<>(responses);
        result.setTotal(pageInfo.getTotal());
        result.setPages(pageInfo.getPages());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(ApprovalRequest request) {
        validateRequest(request);
        if (request.getApplicationId() != null) {
            doApprove(request.getApplicationId(), true, null);
        } else {
            getApplicationIds(request).forEach(id -> doApprove(id, true, null));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchApprove(ApprovalRequest request) {
        List<Long> ids = getApplicationIds(request);
        if (ids.isEmpty()) {
            throw new RuntimeException("请选择要审批的申请");
        }
        ids.forEach(id -> doApprove(id, true, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(ApprovalRequest request) {
        validateRequest(request);
        if (!StringUtils.hasText(request.getRejectReason())) {
            throw new RuntimeException("请填写拒绝原因");
        }
        if (request.getApplicationId() != null) {
            doApprove(request.getApplicationId(), false, request.getRejectReason());
        } else {
            getApplicationIds(request).forEach(id -> doApprove(id, false, request.getRejectReason()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchReject(ApprovalRequest request) {
        List<Long> ids = getApplicationIds(request);
        if (ids.isEmpty()) {
            throw new RuntimeException("请选择要审批的申请");
        }
        if (!StringUtils.hasText(request.getRejectReason())) {
            throw new RuntimeException("请填写拒绝原因");
        }
        ids.forEach(id -> doApprove(id, false, request.getRejectReason()));
    }

    /**
     * 校验审批请求参数
     *
     * @param request 审批请求
     */
    private void validateRequest(ApprovalRequest request) {
        boolean hasId = request.getApplicationId() != null;
        boolean hasIds = request.getApplicationIds() != null && !request.getApplicationIds().isEmpty();
        boolean selectAll = Boolean.TRUE.equals(request.getSelectAll());
        if (!hasId && !hasIds && !selectAll) {
            throw new RuntimeException("请选择要审批的申请");
        }
    }

    /**
     * 获取待审批的申请 ID 列表
     *
     * @param request 审批请求
     * @return 申请 ID 列表
     */
    private List<Long> getApplicationIds(ApprovalRequest request) {
        // 全选：查询当前用户的所有待审批申请
        if (Boolean.TRUE.equals(request.getSelectAll())) {
            Long userId = UserContextHolder.getCurrentUserId();
            return jobApplicationMapper.selectPendingByApproverId(userId).stream()
                    .map(JobApplication::getId)
                    .collect(Collectors.toList());
        }
        // 批量审批：使用指定的 ID 列表
        if (request.getApplicationIds() != null && !request.getApplicationIds().isEmpty()) {
            return request.getApplicationIds();
        }
        // 单个审批
        if (request.getApplicationId() != null) {
            return List.of(request.getApplicationId());
        }
        return List.of();
    }

    /**
     * 执行审批操作（核心逻辑）
     *
     * @param applicationId 申请 ID
     * @param approved true-通过，false-拒绝
     * @param rejectReason 拒绝原因（通过时传 null）
     */
    private void doApprove(Long applicationId, boolean approved, String rejectReason) {
        Long approverId = UserContextHolder.getCurrentUserId();
        String approverName = UserContextHolder.getCurrentUsername();

        JobApplication application = jobApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (application.getStatus() != 0) {
            throw new RuntimeException("该申请已被处理");
        }
        if (!application.getApproverId().equals(approverId)) {
            throw new RuntimeException("您无权审批该申请");
        }

        // 更新申请状态
        application.setStatus(approved ? 1 : 2);
        application.setApproveTime(LocalDateTime.now());
        if (!approved) {
            application.setRejectReason(rejectReason);
        }
        jobApplicationMapper.updateById(application);

        // 发送 MQ 通知
        User applicant = userMapper.selectById(application.getApplicantId());
        ApprovalNotificationMessage notification = ApprovalNotificationMessage.builder()
                .applicationId(applicationId)
                .approverId(approverId)
                .approverName(approverName)
                .applicantId(application.getApplicantId())
                .applicantName(applicant != null ? applicant.getUsername() : "未知")
                .reason(application.getReason())
                .applyTime(application.getApplyTime())
                .messageType(approved ? "APPROVED" : "REJECTED")
                .rejectReason(rejectReason)
                .build();
        notificationService.sendApprovalNotification(notification);

        log.info("审批完成 - id: {}, 结果: {}", applicationId, approved ? "通过" : "拒绝");
    }

    /**
     * 转换为审批响应对象
     *
     * @param application 申请实体
     * @return 审批响应
     */
    private ApprovalResponse convertToResponse(JobApplication application) {
        User applicant = userMapper.selectById(application.getApplicantId());
        String name = applicant != null
                ? (applicant.getRealName() != null ? applicant.getRealName() : applicant.getUsername())
                : "未知";
        return ApprovalResponse.builder()
                .id(application.getId())
                .applicantName(name)
                .reason(application.getReason())
                .applyTime(application.getApplyTime())
                .status(application.getStatus())
                .build();
    }
}