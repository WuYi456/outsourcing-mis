// src/test/java/com/tx/outsourcingmis/service/impl/ApprovalServiceTest.java
package com.tx.outsourcingmis.service.impl;

import com.github.pagehelper.PageInfo;
import com.tx.outsourcingmis.dto.ApprovalRequest;
import com.tx.outsourcingmis.dto.ApprovalResponse;
import com.tx.outsourcingmis.entity.JobApplication;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.JobApplicationMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.ApprovalService;
import com.tx.outsourcingmis.service.NotificationService;
import com.tx.outsourcingmis.utils.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("审批服务单元测试")
class ApprovalServiceTest {

    @Mock
    private JobApplicationMapper jobApplicationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ApprovalService approvalService;

    private JobApplication testApplication;
    private User applicant;
    private User approver;

    @BeforeEach
    void setUp() {
        applicant = new User();
        applicant.setId(100L);
        applicant.setUsername("employee1");
        applicant.setRealName("测试员工");

        approver = new User();
        approver.setId(200L);
        approver.setUsername("leader1");
        approver.setRealName("测试领导");

        testApplication = new JobApplication();
        testApplication.setId(1L);
        testApplication.setApplicantId(100L);
        testApplication.setApproverId(200L);
        testApplication.setReason("希望加入项目组");
        testApplication.setStatus(0);
        testApplication.setRejectReason(null);

        UserContextHolder.setCurrentUserId(200L);
        UserContextHolder.setCurrentUsername("leader1");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("查询待审批列表 - 成功")
    void getPendingList_Success() {
        List<JobApplication> applications = Arrays.asList(testApplication);
        when(jobApplicationMapper.selectPendingByApproverId(200L)).thenReturn(applications);
        when(userMapper.selectById(100L)).thenReturn(applicant);

        PageInfo<ApprovalResponse> result = approvalService.getPendingList(1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
    }

    @Test
    @DisplayName("审批通过 - 成功")
    void approve_Success() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);

        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);
        when(userMapper.selectById(100L)).thenReturn(applicant);
        when(jobApplicationMapper.updateById(any(JobApplication.class))).thenReturn(1);

        approvalService.approve(request);

        verify(jobApplicationMapper, times(1)).updateById(argThat(app -> app.getStatus() == 1));
        verify(notificationService, times(1)).sendApprovalNotification(any());
    }

    @Test
    @DisplayName("审批拒绝 - 成功")
    void reject_Success() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);
        request.setRejectReason("能力不匹配");

        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);
        when(userMapper.selectById(100L)).thenReturn(applicant);
        when(jobApplicationMapper.updateById(any(JobApplication.class))).thenReturn(1);

        approvalService.reject(request);

        verify(jobApplicationMapper, times(1)).updateById(argThat(app ->
                app.getStatus() == 2 && "能力不匹配".equals(app.getRejectReason())));
    }

    @Test
    @DisplayName("审批 - 申请已被处理，应抛出异常")
    void approve_AlreadyProcessed_ThrowsException() {
        testApplication.setStatus(1);
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);

        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);

        assertThatThrownBy(() -> approvalService.approve(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("该申请已被处理");
    }

    @Test
    @DisplayName("审批 - 无权审批他人申请，应抛出异常")
    void approve_NoPermission_ThrowsException() {
        testApplication.setApproverId(999L);
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);

        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);

        assertThatThrownBy(() -> approvalService.approve(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("您无权审批该申请");
    }

    @Test
    @DisplayName("拒绝 - 未填写拒绝原因，应抛出异常")
    void reject_NoReason_ThrowsException() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);
        request.setRejectReason(null);

        assertThatThrownBy(() -> approvalService.reject(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("请填写拒绝原因");
    }

    @Test
    @DisplayName("批量审批通过 - 成功")
    void batchApprove_Success() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationIds(ids);

        JobApplication app2 = new JobApplication();
        app2.setId(2L);
        app2.setApplicantId(100L);
        app2.setApproverId(200L);
        app2.setStatus(0);

        JobApplication app3 = new JobApplication();
        app3.setId(3L);
        app3.setApplicantId(101L);
        app3.setApproverId(200L);
        app3.setStatus(0);

        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);
        when(jobApplicationMapper.selectById(2L)).thenReturn(app2);
        when(jobApplicationMapper.selectById(3L)).thenReturn(app3);
        when(userMapper.selectById(100L)).thenReturn(applicant);
        when(userMapper.selectById(101L)).thenReturn(applicant);
        when(jobApplicationMapper.updateById(any())).thenReturn(1);

        approvalService.batchApprove(request);

        verify(jobApplicationMapper, times(3)).updateById(any());
    }

    @Test
    @DisplayName("批量审批 - 未选择申请，应抛出异常")
    void batchApprove_NoSelection_ThrowsException() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationIds(null);
        request.setSelectAll(false);

        assertThatThrownBy(() -> approvalService.batchApprove(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("请选择要审批的申请");
    }
}