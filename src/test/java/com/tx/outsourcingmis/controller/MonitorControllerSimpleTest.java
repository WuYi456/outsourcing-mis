// src/test/java/com/tx/outsourcingmis/controller/MonitorControllerSimpleTest.java
package com.tx.outsourcingmis.controller;

import com.tx.outsourcingmis.interceptor.JwtInterceptor;
import com.tx.outsourcingmis.interceptor.PermissionInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("监控控制器测试")
class MonitorControllerSimpleTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("JVM信息接口 - 可访问")
    void getJvmInfo() throws Exception {
        mockMvc.perform(get("/api/monitor/jvm/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("JVM内存接口 - 可访问")
    void getJvmMemory() throws Exception {
        mockMvc.perform(get("/api/monitor/jvm/memory"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("线程堆栈接口 - 可访问")
    void getThreadDump() throws Exception {
        mockMvc.perform(get("/api/monitor/thread/dump"))
                .andExpect(status().isOk());
    }
}