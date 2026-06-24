// src/test/java/com/tx/outsourcingmis/controller/PerformanceControllerTest.java
package com.tx.outsourcingmis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tx.outsourcingmis.dto.PerformanceEvaluateRequest;
import com.tx.outsourcingmis.dto.PerformanceResponse;
import com.tx.outsourcingmis.interceptor.JwtInterceptor;
import com.tx.outsourcingmis.interceptor.PermissionInterceptor;
import com.tx.outsourcingmis.service.PerformanceService;
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
@DisplayName("绩效控制器测试")
class PerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PerformanceService performanceService;

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
    @DisplayName("评定绩效 - 成功")
    void evaluate_Success() throws Exception {
        PerformanceEvaluateRequest request = new PerformanceEvaluateRequest();
        request.setUserId(100L);
        request.setGrade("B");
        request.setComment("工作表现良好");
        request.setEvaluateYear(2026);
        request.setEvaluateMonth(6);

        PerformanceResponse response = PerformanceResponse.builder()
                .id(1L)
                .userId(100L)
                .userName("测试员工")
                .grade("B")
                .gradeDesc("良好 - 超出预期")
                .comment("工作表现良好")
                .evaluateYear(2026)
                .evaluateMonth(6)
                .status(0)
                .statusDesc("待确认")
                .createTime(LocalDateTime.now())
                .build();

        when(performanceService.evaluate(any(PerformanceEvaluateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/performance/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(performanceService, times(1)).evaluate(any(PerformanceEvaluateRequest.class));
    }

    @Test
    @DisplayName("评定绩效 - 参数校验失败（缺少用户ID）")
    void evaluate_MissingUserId() throws Exception {
        PerformanceEvaluateRequest request = new PerformanceEvaluateRequest();
        request.setGrade("B");
        request.setEvaluateYear(2026);
        request.setEvaluateMonth(6);

        mockMvc.perform(post("/api/performance/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }


    @Test
    @DisplayName("确认绩效 - 成功")
    void confirmPerformance_Success() throws Exception {
        doNothing().when(performanceService).confirmPerformance(1L);

        mockMvc.perform(put("/api/performance/confirm/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(performanceService, times(1)).confirmPerformance(1L);
    }

    @Test
    @DisplayName("获取用户绩效 - 成功")
    void getUserPerformances_Success() throws Exception {
        PerformanceResponse response = PerformanceResponse.builder()
                .id(1L)
                .grade("B")
                .evaluateYear(2026)
                .evaluateMonth(6)
                .build();

        when(performanceService.getUserPerformances(100L)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/performance/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(performanceService, times(1)).getUserPerformances(100L);
    }

    @Test
    @DisplayName("获取某月所有人员绩效 - 成功")
    void getPerformancesByPeriod_Success() throws Exception {
        PerformanceResponse response = PerformanceResponse.builder()
                .id(1L)
                .userName("测试员工")
                .grade("B")
                .evaluateYear(2026)
                .evaluateMonth(6)
                .build();

        when(performanceService.getPerformancesByPeriod(2026, 6)).thenReturn(Arrays.asList(response));

        mockMvc.perform(get("/api/performance/period")
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(performanceService, times(1)).getPerformancesByPeriod(2026, 6);
    }
}