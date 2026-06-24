// src/test/java/com/tx/outsourcingmis/utils/JwtUtilsTest.java
package com.tx.outsourcingmis.utils;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JWT工具类单元测试")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret",
                "dGhpcy1pcy1hLXNlY3VyZS1rZXktZm9yLWhzNTEyLXdpdGgtZW5vdWdoLWxlbmd0aC1nZW5lcmF0ZWQtYXV0b21hdGljYWxseQo=");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 7200000L);
    }

    @Test
    @DisplayName("生成Token - 成功")
    void generateToken_Success() {
        List<String> permissions = Arrays.asList("user:view", "job:submit", "performance:view");

        String token = jwtUtils.generateToken(1L, "testuser", "ADMIN", permissions);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("生成Token并解析用户ID - 成功")
    void getUserIdFromToken_Success() {
        String token = jwtUtils.generateToken(999L, "user999", "EMPLOYEE", List.of());

        Long userId = jwtUtils.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(999L);
    }

    @Test
    @DisplayName("生成Token并解析用户名 - 成功")
    void getUsernameFromToken_Success() {
        String token = jwtUtils.generateToken(1L, "john_doe", "MANAGER", List.of());

        String username = jwtUtils.getUsernameFromToken(token);

        assertThat(username).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("生成Token并解析角色 - 成功")
    void getRoleFromToken_Success() {
        String token = jwtUtils.generateToken(1L, "testuser", "ADMIN", List.of());

        String role = jwtUtils.getRoleFromToken(token);

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("生成Token并解析权限列表 - 成功")
    void getPermissionsFromToken_Success() {
        List<String> expectedPermissions = Arrays.asList("user:view", "user:edit", "job:submit");
        String token = jwtUtils.generateToken(1L, "testuser", "ADMIN", expectedPermissions);

        List<String> permissions = jwtUtils.getPermissionsFromToken(token);

        assertThat(permissions).containsExactlyInAnyOrder("user:view", "user:edit", "job:submit");
    }

    @Test
    @DisplayName("验证Token - 有效Token返回true")
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtils.generateToken(1L, "testuser", "EMPLOYEE", List.of());

        boolean isValid = jwtUtils.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("验证Token - 无效Token返回false")
    void validateToken_InvalidToken_ReturnsFalse() {
        boolean isValid = jwtUtils.validateToken("invalid.token.string");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("验证Token - 空Token返回false")
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtUtils.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("验证Token - 过期Token返回false")
    void validateToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtils, "expiration", 1000L);
        String token = jwtUtils.generateToken(1L, "testuser", "EMPLOYEE", List.of());

        TimeUnit.MILLISECONDS.sleep(1500);

        boolean isValid = jwtUtils.validateToken(token);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("解析过期Token - 抛出异常")
    void parseExpiredToken_ThrowsException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtils, "expiration", 500L);
        String token = jwtUtils.generateToken(1L, "testuser", "EMPLOYEE", List.of());

        TimeUnit.MILLISECONDS.sleep(1000);

        assertThatThrownBy(() -> jwtUtils.getUserIdFromToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}