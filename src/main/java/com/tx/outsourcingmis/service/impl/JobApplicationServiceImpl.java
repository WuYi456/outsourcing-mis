package com.tx.outsourcingmis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tx.outsourcingmis.dto.JobApplicationRequest;
import com.tx.outsourcingmis.dto.JobApplicationResponse;
import com.tx.outsourcingmis.entity.JobApplication;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.JobApplicationMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.JobApplicationService;
import com.tx.outsourcingmis.utils.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 上岗申请服务实现类
 *
 * <p>核心功能：
 * <ul>
 *   <li>提交上岗申请（自动关联上级领导作为审批人）</li>
 *   <li>查询本人申请列表</li>
 *   <li>查看申请详情</li>
 *   <li>取消申请（仅待审批状态可取消）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationMapper jobApplicationMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobApplicationResponse submitApplication(JobApplicationRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new RuntimeException("当前用户不存在");
        }

        // 获取上级领导作为审批人
        Long leaderId = currentUser.getLeaderId();
        if (leaderId == null) {
            throw new RuntimeException("未配置上级领导，无法提交申请");
        }

        // 检查是否有待审批的申请（不允许重复提交）
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplication::getApplicantId, userId)
                .eq(JobApplication::getStatus, 0);
        if (jobApplicationMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已有待审批的申请，请等待审批完成后再提交");
        }

        // 创建申请记录
        JobApplication application = new JobApplication();
        application.setApplicantId(userId);
        application.setApproverId(leaderId);
        application.setReason(request.getReason());
        application.setRemark(request.getRemark());
        application.setStatus(0);
        application.setApplyTime(LocalDateTime.now());

        if (jobApplicationMapper.insert(application) <= 0) {
            throw new RuntimeException("提交申请失败");
        }

        log.info("提交申请成功 - userId: {}, applicationId: {}", userId, application.getId());
        return convertToResponse(application);
    }

    @Override
    public List<JobApplicationResponse> getMyApplications() {
        Long userId = UserContextHolder.getCurrentUserId();
        return jobApplicationMapper.selectByApplicantId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public JobApplicationResponse getApplicationDetail(Long id) {
        Long userId = UserContextHolder.getCurrentUserId();
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        // 只能查看自己的申请
        if (!application.getApplicantId().equals(userId)) {
            throw new RuntimeException("无权查看他人申请");
        }
        return convertToResponse(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(Long id) {
        Long userId = UserContextHolder.getCurrentUserId();
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        // 只能取消自己的申请
        if (!application.getApplicantId().equals(userId)) {
            throw new RuntimeException("无权取消他人申请");
        }
        // 只有待审批状态可以取消
        if (application.getStatus() != 0) {
            throw new RuntimeException("当前状态为【" + getStatusDesc(application.getStatus()) + "】，无法取消");
        }
        if (jobApplicationMapper.deleteById(id) <= 0) {
            throw new RuntimeException("取消申请失败");
        }
        log.info("取消申请成功 - userId: {}, applicationId: {}", userId, id);
    }

    @Override
    public JobApplication getById(Long id) {
        return jobApplicationMapper.selectById(id);
    }

    /**
     * 将申请实体转换为响应对象
     *
     * @param application 申请实体
     * @return 申请响应
     */
    private JobApplicationResponse convertToResponse(JobApplication application) {
        User applicant = userMapper.selectById(application.getApplicantId());
        String applicantName = getUserName(applicant);

        String approverName = null;
        if (application.getApproverId() != null) {
            approverName = getUserName(userMapper.selectById(application.getApproverId()));
        }

        return JobApplicationResponse.builder()
                .id(application.getId())
                .applicantId(application.getApplicantId())
                .applicantName(applicantName)
                .approverId(application.getApproverId())
                .approverName(approverName)
                .reason(application.getReason())
                .status(application.getStatus())
                .statusDesc(getStatusDesc(application.getStatus()))
                .rejectReason(application.getRejectReason())
                .remark(application.getRemark())
                .applyTime(application.getApplyTime())
                .approveTime(application.getApproveTime())
                .build();
    }

    /**
     * 获取用户显示名称（优先使用真实姓名）
     *
     * @param user 用户实体
     * @return 显示名称
     */
    private String getUserName(User user) {
        if (user == null) {
            return "未知";
        }
        return user.getRealName() != null ? user.getRealName() : user.getUsername();
    }

    /**
     * 获取状态中文描述
     *
     * @param status 状态码
     * @return 中文描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审批";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }
}