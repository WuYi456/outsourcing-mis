// src/test/java/com/tx/outsourcingmis/dto/DtoTests.java
package com.tx.outsourcingmis.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO类测试")
class DtoTests {

    @Test
    @DisplayName("LoginRequest测试")
    void testLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        assertThat(request.getUsername()).isEqualTo("testuser");
        assertThat(request.getPassword()).isEqualTo("123456");
    }

    @Test
    @DisplayName("LoginResponse测试")
    void testLoginResponse() {
        LoginResponse response = LoginResponse.builder()
                .token("jwt.token")
                .userId(1L)
                .username("testuser")
                .role("EMPLOYEE")
                .permissions(Arrays.asList("user:view", "job:submit"))
                .build();

        assertThat(response.getToken()).isEqualTo("jwt.token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("EMPLOYEE");
        assertThat(response.getPermissions()).hasSize(2);
    }

    @Test
    @DisplayName("ApprovalRequest测试")
    void testApprovalRequest() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);
        request.setApplicationIds(Arrays.asList(1L, 2L, 3L));
        request.setSelectAll(true);
        request.setRejectReason("原因");

        assertThat(request.getApplicationId()).isEqualTo(1L);
        assertThat(request.getApplicationIds()).hasSize(3);
        assertThat(request.getSelectAll()).isTrue();
        assertThat(request.getRejectReason()).isEqualTo("原因");
    }

    @Test
    @DisplayName("ApprovalResponse测试")
    void testApprovalResponse() {
        ApprovalResponse response = ApprovalResponse.builder()
                .id(1L)
                .applicantName("测试员工")
                .reason("申请理由")
                .applyTime(LocalDateTime.now())
                .status(0)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getApplicantName()).isEqualTo("测试员工");
        assertThat(response.getReason()).isEqualTo("申请理由");
        assertThat(response.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("JobApplicationRequest测试")
    void testJobApplicationRequest() {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setReason("申请理由");
        request.setRemark("备注");

        assertThat(request.getReason()).isEqualTo("申请理由");
        assertThat(request.getRemark()).isEqualTo("备注");
    }

    @Test
    @DisplayName("JobApplicationResponse测试")
    void testJobApplicationResponse() {
        JobApplicationResponse response = JobApplicationResponse.builder()
                .id(1L)
                .applicantId(100L)
                .applicantName("测试员工")
                .reason("申请理由")
                .status(0)
                .statusDesc("待审批")
                .applyTime(LocalDateTime.now())
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getApplicantId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(0);
        assertThat(response.getStatusDesc()).isEqualTo("待审批");
    }

    @Test
    @DisplayName("PerformanceEvaluateRequest测试")
    void testPerformanceEvaluateRequest() {
        PerformanceEvaluateRequest request = new PerformanceEvaluateRequest();
        request.setUserId(100L);
        request.setGrade("B");
        request.setComment("表现良好");
        request.setEvaluateYear(2026);
        request.setEvaluateMonth(6);

        assertThat(request.getUserId()).isEqualTo(100L);
        assertThat(request.getGrade()).isEqualTo("B");
        assertThat(request.getComment()).isEqualTo("表现良好");
        assertThat(request.getEvaluateYear()).isEqualTo(2026);
        assertThat(request.getEvaluateMonth()).isEqualTo(6);
    }

    @Test
    @DisplayName("PerformanceResponse测试")
    void testPerformanceResponse() {
        PerformanceResponse response = PerformanceResponse.builder()
                .id(1L)
                .userId(100L)
                .userName("测试员工")
                .grade("B")
                .gradeDesc("良好")
                .comment("表现良好")
                .evaluateYear(2026)
                .evaluateMonth(6)
                .status(0)
                .statusDesc("待确认")
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getGrade()).isEqualTo("B");
        assertThat(response.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("OperationLogQueryRequest测试")
    void testOperationLogQueryRequest() {
        OperationLogQueryRequest request = new OperationLogQueryRequest();
        request.setUsername("testuser");
        request.setOperation("登录");
        request.setStartTime("2026-01-01 00:00:00");
        request.setEndTime("2026-12-31 23:59:59");
        request.setPageNum(2);
        request.setPageSize(50);

        assertThat(request.getUsername()).isEqualTo("testuser");
        assertThat(request.getPageNum()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("OperationLogResponse测试")
    void testOperationLogResponse() {
        OperationLogResponse response = OperationLogResponse.builder()
                .id("log-1")
                .userId(100L)
                .username("testuser")
                .operation("登录")
                .duration(100L)
                .success(true)
                .createTime(LocalDateTime.now())
                .build();

        assertThat(response.getId()).isEqualTo("log-1");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getDuration()).isEqualTo(100L);
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    @DisplayName("ApprovalNotificationMessage测试")
    void testApprovalNotificationMessage() {
        ApprovalNotificationMessage message = ApprovalNotificationMessage.builder()
                .applicationId(1L)
                .approverId(200L)
                .approverName("领导")
                .applicantId(100L)
                .applicantName("员工")
                .reason("申请理由")
                .applyTime(LocalDateTime.now())
                .messageType("PENDING")
                .rejectReason(null)
                .build();

        assertThat(message.getApplicationId()).isEqualTo(1L);
        assertThat(message.getMessageType()).isEqualTo("PENDING");
        assertThat(message.getApplicantName()).isEqualTo("员工");
    }
}