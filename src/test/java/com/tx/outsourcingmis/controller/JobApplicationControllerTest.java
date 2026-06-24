// src/test/java/com/tx/outsourcingmis/controller/JobApplicationControllerTest.java
package com.tx.outsourcingmis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tx.outsourcingmis.dto.JobApplicationRequest;
import com.tx.outsourcingmis.dto.JobApplicationResponse;
import com.tx.outsourcingmis.interceptor.JwtInterceptor;
import com.tx.outsourcingmis.interceptor.PermissionInterceptor;
import com.tx.outsourcingmis.service.JobApplicationService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("上岗申请控制器测试")
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobApplicationService jobApplicationService;

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
    @DisplayName("提交上岗申请 - 成功")
    void submitApplication_Success() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setReason("希望加入项目组");
        request.setRemark("有3年Java经验");

        JobApplicationResponse response = JobApplicationResponse.builder()
                .id(1L)
                .applicantId(100L)
                .applicantName("测试员工")
                .reason("希望加入项目组")
                .status(0)
                .statusDesc("待审批")
                .applyTime(LocalDateTime.now())
                .build();

        when(jobApplicationService.submitApplication(any(JobApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/job-application/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(jobApplicationService, times(1)).submitApplication(any(JobApplicationRequest.class));
    }

    @Test
    @DisplayName("提交上岗申请 - 参数校验失败（空理由）")
    void submitApplication_EmptyReason() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setReason("");

        mockMvc.perform(post("/api/job-application/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("获取我的申请列表 - 成功")
    void getMyApplications_Success() throws Exception {
        JobApplicationResponse response = JobApplicationResponse.builder()
                .id(1L)
                .reason("申请理由")
                .status(0)
                .statusDesc("待审批")
                .applyTime(LocalDateTime.now())
                .build();

        when(jobApplicationService.getMyApplications()).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/job-application/my-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(jobApplicationService, times(1)).getMyApplications();
    }

    @Test
    @DisplayName("获取申请详情 - 成功")
    void getApplicationDetail_Success() throws Exception {
        JobApplicationResponse response = JobApplicationResponse.builder()
                .id(1L)
                .applicantId(100L)
                .applicantName("测试员工")
                .reason("申请理由")
                .status(0)
                .statusDesc("待审批")
                .applyTime(LocalDateTime.now())
                .build();

        when(jobApplicationService.getApplicationDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/api/job-application/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(jobApplicationService, times(1)).getApplicationDetail(1L);
    }

    @Test
    @DisplayName("取消申请 - 成功")
    void cancelApplication_Success() throws Exception {
        doNothing().when(jobApplicationService).cancelApplication(1L);

        mockMvc.perform(delete("/api/job-application/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(jobApplicationService, times(1)).cancelApplication(1L);
    }
}