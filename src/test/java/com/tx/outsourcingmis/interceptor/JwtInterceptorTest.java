// src/test/java/com/tx/outsourcingmis/interceptor/JwtInterceptorTest.java
package com.tx.outsourcingmis.interceptor;

import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.service.CacheService;
import com.tx.outsourcingmis.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT拦截器测试")
class JwtInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private JwtInterceptor jwtInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("无Token - 放行")
    void preHandle_NoToken_ReturnsTrue() throws Exception {
        boolean result = jwtInterceptor.preHandle(request, response, null);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("有效Token - 从缓存获取用户信息")
    void preHandle_ValidToken_FromCache() throws Exception {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        LoginResponse loginResponse = LoginResponse.builder()
                .userId(1L)
                .username("testuser")
                .role("EMPLOYEE")
                .permissions(Arrays.asList("user:view", "job:submit"))
                .build();

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(cacheService.isTokenValid(token)).thenReturn(true);
        when(cacheService.getLoginResponseByToken(token)).thenReturn(loginResponse);

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertThat(result).isTrue();
        verify(jwtUtils, never()).getUserIdFromToken(anyString());
    }

    @Test
    @DisplayName("有效Token - 从JWT解析用户信息（缓存未命中）")
    void preHandle_ValidToken_FromJwt() throws Exception {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(cacheService.isTokenValid(token)).thenReturn(true);
        when(cacheService.getLoginResponseByToken(token)).thenReturn(null);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(1L);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtUtils.getRoleFromToken(token)).thenReturn("EMPLOYEE");
        when(jwtUtils.getPermissionsFromToken(token)).thenReturn(Arrays.asList("user:view"));

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertThat(result).isTrue();
        verify(jwtUtils, times(1)).getUserIdFromToken(token);
    }

    @Test
    @DisplayName("无效Token - 抛出异常")
    void preHandle_InvalidToken_ThrowsException() {
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(false);

        assertThatThrownBy(() -> jwtInterceptor.preHandle(request, response, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token无效或已过期");
    }

    @Test
    @DisplayName("Token已被登出 - 抛出异常")
    void preHandle_TokenInvalidated_ThrowsException() {
        String token = "loggedout.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(cacheService.isTokenValid(token)).thenReturn(false);

        assertThatThrownBy(() -> jwtInterceptor.preHandle(request, response, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token已失效，请重新登录");
    }

    @Test
    @DisplayName("afterCompletion - 清除ThreadLocal")
    void afterCompletion_ClearsThreadLocal() throws Exception {
        // 先调用 preHandle 设置 ThreadLocal
        String token = "valid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(cacheService.isTokenValid(token)).thenReturn(true);
        when(cacheService.getLoginResponseByToken(token)).thenReturn(null);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(1L);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtUtils.getRoleFromToken(token)).thenReturn("EMPLOYEE");
        when(jwtUtils.getPermissionsFromToken(token)).thenReturn(Arrays.asList("user:view"));

        jwtInterceptor.preHandle(request, response, null);

        // 调用 afterCompletion
        jwtInterceptor.afterCompletion(request, response, null, null);

        // 验证没有异常抛出
    }

    @Test
    @DisplayName("提取Token - Bearer格式")
    void extractToken_BearerFormat() throws Exception {
        String token = "bearer.token.123";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(cacheService.isTokenValid(token)).thenReturn(true);
        when(cacheService.getLoginResponseByToken(token)).thenReturn(null);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(1L);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtUtils.getRoleFromToken(token)).thenReturn("EMPLOYEE");
        when(jwtUtils.getPermissionsFromToken(token)).thenReturn(Arrays.asList("user:view"));

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertThat(result).isTrue();
        verify(jwtUtils).validateToken(token);
    }
}