// src/test/java/com/tx/outsourcingmis/controller/ApprovalControllerTest.java
package com.tx.outsourcingmis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.tx.outsourcingmis.dto.ApprovalRequest;
import com.tx.outsourcingmis.dto.ApprovalResponse;
import com.tx.outsourcingmis.interceptor.JwtInterceptor;
import com.tx.outsourcingmis.interceptor.PermissionInterceptor;
import com.tx.outsourcingmis.service.ApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("审批控制器测试")
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalService approvalService;

    @MockBean
    private JwtInterceptor jwtInterceptor;

    @MockBean
    private PermissionInterceptor permissionInterceptor;

    @BeforeEach
    void setUp() {
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(permissionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("测试接口 - 成功")
    void test() throws Exception {
        mockMvc.perform(get("/api/approval/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("Controller is working!"));
    }

    @Test
    @DisplayName("查询待审批列表 - 成功")
    void getPendingList_Success() throws Exception {
        ApprovalResponse response = ApprovalResponse.builder()
                .id(1L)
                .applicantName("测试员工")
                .reason("申请理由")
                .applyTime(LocalDateTime.now())
                .status(0)
                .build();

        PageInfo<ApprovalResponse> pageInfo = new PageInfo<>(Collections.singletonList(response));
        when(approvalService.getPendingList(1, 10)).thenReturn(pageInfo);

        mockMvc.perform(get("/api/approval/pending")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(approvalService, times(1)).getPendingList(1, 10);
    }

    @Test
    @DisplayName("查询待审批列表 - 默认参数")
    void getPendingList_DefaultParams() throws Exception {
        ApprovalResponse response = ApprovalResponse.builder()
                .id(1L)
                .applicantName("测试员工")
                .reason("申请理由")
                .applyTime(LocalDateTime.now())
                .status(0)
                .build();

        PageInfo<ApprovalResponse> pageInfo = new PageInfo<>(Collections.singletonList(response));
        when(approvalService.getPendingList(1, 10)).thenReturn(pageInfo);

        mockMvc.perform(get("/api/approval/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审批通过 - 成功")
    void approve_Success() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);

        doNothing().when(approvalService).approve(any(ApprovalRequest.class));

        mockMvc.perform(post("/api/approval/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(approvalService, times(1)).approve(any(ApprovalRequest.class));
    }

    @Test
    @DisplayName("批量审批通过 - 成功")
    void batchApprove_Success() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationIds(Arrays.asList(1L, 2L, 3L));

        doNothing().when(approvalService).batchApprove(any(ApprovalRequest.class));

        mockMvc.perform(post("/api/approval/batch-approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(approvalService, times(1)).batchApprove(any(ApprovalRequest.class));
    }

    @Test
    @DisplayName("批量审批通过 - 全选模式")
    void batchApprove_SelectAll() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setSelectAll(true);

        doNothing().when(approvalService).batchApprove(any(ApprovalRequest.class));

        mockMvc.perform(post("/api/approval/batch-approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审批拒绝 - 成功")
    void reject_Success() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationId(1L);
        request.setRejectReason("能力不匹配");

        doNothing().when(approvalService).reject(any(ApprovalRequest.class));

        mockMvc.perform(post("/api/approval/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(approvalService, times(1)).reject(any(ApprovalRequest.class));
    }

    @Test
    @DisplayName("批量审批拒绝 - 成功")
    void batchReject_Success() throws Exception {
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationIds(Arrays.asList(1L, 2L, 3L));
        request.setRejectReason("能力不匹配");

        doNothing().when(approvalService).batchReject(any(ApprovalRequest.class));

        mockMvc.perform(post("/api/approval/batch-reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(approvalService, times(1)).batchReject(any(ApprovalRequest.class));
    }
}