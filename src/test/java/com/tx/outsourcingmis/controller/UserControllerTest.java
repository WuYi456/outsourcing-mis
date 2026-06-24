// src/test/java/com/tx/outsourcingmis/controller/UserControllerTest.java
package com.tx.outsourcingmis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tx.outsourcingmis.dto.LoginRequest;
import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.interceptor.JwtInterceptor;
import com.tx.outsourcingmis.interceptor.PermissionInterceptor;
import com.tx.outsourcingmis.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("用户控制器测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // Mock掉拦截器，让它们直接放行
    @MockBean
    private JwtInterceptor jwtInterceptor;

    @MockBean
    private PermissionInterceptor permissionInterceptor;

    private User testUser;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setRole("EMPLOYEE");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        loginResponse = LoginResponse.builder()
                .token("jwt.token.test")
                .userId(1L)
                .username("testuser")
                .role("EMPLOYEE")
                .permissions(Arrays.asList("user:view", "job:submit"))
                .build();

        // 让拦截器直接放行
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(permissionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void register_Success() throws Exception {
        doNothing().when(userService).register(any(User.class));

        // 使用 Map 确保 JSON 包含 password 字段
        java.util.Map<String, String> registerUser = new java.util.HashMap<>();
        registerUser.put("username", "newuser");
        registerUser.put("password", "password123");
        registerUser.put("email", "newuser@example.com");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(userService, times(1)).register(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 参数校验失败（空用户名）")
    void register_ValidationFailed_EmptyUsername() throws Exception {
        java.util.Map<String, String> invalidUser = new java.util.HashMap<>();
        invalidUser.put("username", "");
        invalidUser.put("password", "password123");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt.token.test"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("用户登录 - 参数校验失败（空用户名）")
    void login_ValidationFailed_EmptyUsername() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("用户登录 - 业务异常")
    void login_BusinessException() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("用户名或密码错误"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户登出 - 成功")
    void logout_Success() throws Exception {
        doNothing().when(userService).logout();

        mockMvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).logout();
    }

    @Test
    @DisplayName("获取当前用户信息 - 成功")
    void getCurrentUser_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/api/user/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("根据用户名获取用户信息 - 成功")
    void getUserByUsername_Success() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(get("/api/user/info/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService, times(1)).getUserByUsername("testuser");
    }
}