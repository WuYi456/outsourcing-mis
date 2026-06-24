// src/test/java/com/tx/outsourcingmis/service/impl/JobApplicationServiceImplTest.java
package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.dto.JobApplicationRequest;
import com.tx.outsourcingmis.dto.JobApplicationResponse;
import com.tx.outsourcingmis.entity.JobApplication;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.JobApplicationMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.utils.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("上岗申请服务单元测试")
class JobApplicationServiceImplTest {

    @Mock
    private JobApplicationMapper jobApplicationMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    private User currentUser;
    private User leaderUser;
    private JobApplication testApplication;
    private JobApplicationRequest request;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(100L);
        currentUser.setUsername("employee1");
        currentUser.setRealName("测试员工");
        currentUser.setLeaderId(200L);

        leaderUser = new User();
        leaderUser.setId(200L);
        leaderUser.setUsername("leader1");
        leaderUser.setRealName("测试领导");

        testApplication = new JobApplication();
        testApplication.setId(1L);
        testApplication.setApplicantId(100L);
        testApplication.setApproverId(200L);
        testApplication.setReason("希望加入项目组");
        testApplication.setStatus(0);
        testApplication.setApplyTime(LocalDateTime.now());

        request = new JobApplicationRequest();
        request.setReason("希望加入项目组");
        request.setRemark("有3年Java经验");

        UserContextHolder.setCurrentUserId(100L);
        UserContextHolder.setCurrentUsername("employee1");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("提交上岗申请 - 成功")
    void submitApplication_Success() {
        when(userMapper.selectById(100L)).thenReturn(currentUser);
        when(jobApplicationMapper.selectCount(any())).thenReturn(0L);
        when(jobApplicationMapper.insert(any(JobApplication.class))).thenReturn(1);
        when(userMapper.selectById(200L)).thenReturn(leaderUser);

        JobApplicationResponse response = jobApplicationService.submitApplication(request);

        assertThat(response).isNotNull();
        assertThat(response.getReason()).isEqualTo("希望加入项目组");
        assertThat(response.getStatus()).isEqualTo(0);

        verify(jobApplicationMapper, times(1)).insert(any(JobApplication.class));
    }

    @Test
    @DisplayName("提交上岗申请 - 当前用户不存在，应抛出异常")
    void submitApplication_UserNotFound_ThrowsException() {
        when(userMapper.selectById(100L)).thenReturn(null);

        assertThatThrownBy(() -> jobApplicationService.submitApplication(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("当前用户不存在");
    }

    @Test
    @DisplayName("提交上岗申请 - 未配置上级领导，应抛出异常")
    void submitApplication_NoLeader_ThrowsException() {
        currentUser.setLeaderId(null);
        when(userMapper.selectById(100L)).thenReturn(currentUser);

        assertThatThrownBy(() -> jobApplicationService.submitApplication(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("未配置上级领导，无法提交申请");
    }

    @Test
    @DisplayName("提交上岗申请 - 已有待审批申请，应抛出异常")
    void submitApplication_ExistingPending_ThrowsException() {
        when(userMapper.selectById(100L)).thenReturn(currentUser);
        when(jobApplicationMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> jobApplicationService.submitApplication(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("您已有待审批的申请，请等待审批完成后再提交");
    }

    @Test
    @DisplayName("获取我的申请列表 - 成功")
    void getMyApplications_Success() {
        List<JobApplication> mockList = List.of(testApplication);
        when(jobApplicationMapper.selectByApplicantId(100L)).thenReturn(mockList);
        when(userMapper.selectById(100L)).thenReturn(currentUser);
        when(userMapper.selectById(200L)).thenReturn(leaderUser);

        List<JobApplicationResponse> responses = jobApplicationService.getMyApplications();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getApplicantName()).isEqualTo("测试员工");
    }

    @Test
    @DisplayName("获取申请详情 - 成功")
    void getApplicationDetail_Success() {
        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);
        when(userMapper.selectById(100L)).thenReturn(currentUser);
        when(userMapper.selectById(200L)).thenReturn(leaderUser);

        JobApplicationResponse response = jobApplicationService.getApplicationDetail(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getReason()).isEqualTo("希望加入项目组");
    }

    @Test
    @DisplayName("获取申请详情 - 无权查看他人申请")
    void getApplicationDetail_NoPermission() {
        UserContextHolder.setCurrentUserId(999L); // 不同用户
        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);

        assertThatThrownBy(() -> jobApplicationService.getApplicationDetail(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("无权查看他人申请");
    }

    @Test
    @DisplayName("取消申请 - 成功")
    void cancelApplication_Success() {
        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);
        when(jobApplicationMapper.deleteById(1L)).thenReturn(1);

        jobApplicationService.cancelApplication(1L);

        verify(jobApplicationMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("取消申请 - 申请状态非待审批，应抛出异常")
    void cancelApplication_NotPending_ThrowsException() {
        testApplication.setStatus(1);
        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);

        assertThatThrownBy(() -> jobApplicationService.cancelApplication(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无法取消");
    }

    @Test
    @DisplayName("取消申请 - 无权取消他人申请，应抛出异常")
    void cancelApplication_NoPermission_ThrowsException() {
        testApplication.setApplicantId(999L);
        when(jobApplicationMapper.selectById(1L)).thenReturn(testApplication);

        assertThatThrownBy(() -> jobApplicationService.cancelApplication(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("无权取消他人申请");
    }
}