// src/test/java/com/tx/outsourcingmis/controller/OperationLogControllerTest.java
package com.tx.outsourcingmis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tx.outsourcingmis.dto.OperationLogQueryRequest;
import com.tx.outsourcingmis.dto.OperationLogResponse;
import com.tx.outsourcingmis.interceptor.JwtInterceptor;
import com.tx.outsourcingmis.interceptor.PermissionInterceptor;
import com.tx.outsourcingmis.service.OperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("操作日志控制器测试")
class OperationLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OperationLogService operationLogService;

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
    @DisplayName("高级搜索操作日志 - 成功")
    void search_Success() throws Exception {
        OperationLogQueryRequest request = new OperationLogQueryRequest();
        request.setUsername("testuser");
        request.setPageNum(1);
        request.setPageSize(20);

        OperationLogResponse response = OperationLogResponse.builder()
                .id("1")
                .username("testuser")
                .operation("登录")
                .duration(100L)
                .success(true)
                .createTime(LocalDateTime.now())
                .build();

        Page<OperationLogResponse> page = new PageImpl<>(
                Collections.singletonList(response),
                PageRequest.of(0, 20),
                1
        );

        when(operationLogService.search(any(OperationLogQueryRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/log/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).search(any(OperationLogQueryRequest.class));
    }

    @Test
    @DisplayName("根据用户名查询操作日志 - 成功")
    void getByUsername_Success() throws Exception {
        OperationLogResponse response = OperationLogResponse.builder()
                .id("1")
                .username("testuser")
                .operation("登录")
                .build();

        Page<OperationLogResponse> page = new PageImpl<>(
                Collections.singletonList(response),
                PageRequest.of(0, 20),
                1
        );

        when(operationLogService.getByUsername(eq("testuser"), eq(1), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/log/user/testuser")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).getByUsername("testuser", 1, 20);
    }

    @Test
    @DisplayName("根据操作类型查询操作日志 - 成功")
    void getByOperation_Success() throws Exception {
        OperationLogResponse response = OperationLogResponse.builder()
                .id("1")
                .operation("登录")
                .build();

        Page<OperationLogResponse> page = new PageImpl<>(
                Collections.singletonList(response),
                PageRequest.of(0, 20),
                1
        );

        when(operationLogService.getByOperation(eq("登录"), eq(1), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/log/operation/登录")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("根据ID查询日志详情 - 成功")
    void getById_Success() throws Exception {
        com.tx.outsourcingmis.es.document.OperationLogDocument doc =
                com.tx.outsourcingmis.es.document.OperationLogDocument.builder()
                        .id("1")
                        .username("testuser")
                        .operation("登录")
                        .build();

        when(operationLogService.getById("1")).thenReturn(doc);

        mockMvc.perform(get("/api/log/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除操作日志 - 成功")
    void deleteById_Success() throws Exception {
        doNothing().when(operationLogService).deleteById("1");

        mockMvc.perform(delete("/api/log/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(operationLogService, times(1)).deleteById("1");
    }
}