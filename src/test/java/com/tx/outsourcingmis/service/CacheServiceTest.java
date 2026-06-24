// src/test/java/com/tx/outsourcingmis/service/CacheServiceTest.java
package com.tx.outsourcingmis.service;

import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("缓存服务单元测试")
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CacheServiceImpl cacheService;

    private User testUser;
    private LoginResponse loginResponse;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setRole("EMPLOYEE");

        loginResponse = LoginResponse.builder()
                .token("test.token.123")
                .userId(1L)
                .username("testuser")
                .role("EMPLOYEE")
                .permissions(Arrays.asList("user:view", "job:submit"))
                .build();

        testToken = "test.token.123";

        ReflectionTestUtils.setField(cacheService, "userPrefix", "user:");
        ReflectionTestUtils.setField(cacheService, "tokenPrefix", "token:");
        ReflectionTestUtils.setField(cacheService, "sessionTtl", 7200L);
    }

    @Test
    @DisplayName("缓存用户登录信息 - 成功")
    void cacheUserLogin_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.cacheUserLogin(testToken, loginResponse, 1L);

        verify(valueOperations, times(2)).set(anyString(), any(), eq(7200L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("根据Token获取登录信息 - 缓存命中")
    void getLoginResponseByToken_CacheHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("token:test.token.123"))).thenReturn(loginResponse);
        when(redisTemplate.expire(anyString(), eq(7200L), eq(TimeUnit.SECONDS))).thenReturn(true);

        LoginResponse result = cacheService.getLoginResponseByToken(testToken);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getToken()).isEqualTo("test.token.123");
    }

    @Test
    @DisplayName("根据Token获取登录信息 - 缓存未命中")
    void getLoginResponseByToken_CacheMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("token:test.token.123"))).thenReturn(null);

        LoginResponse result = cacheService.getLoginResponseByToken(testToken);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("获取用户信息 - 缓存命中")
    void getUserById_CacheHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("user:1"))).thenReturn(testUser);

        User result = cacheService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("获取用户信息 - 缓存未命中，从数据库查询")
    void getUserById_CacheMiss_FromDB() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("user:1"))).thenReturn(null);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // void方法使用 doNothing() 或 doReturn()
        doNothing().when(valueOperations).set(eq("user:1"), eq(testUser), eq(7200L), eq(TimeUnit.SECONDS));

        User result = cacheService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("缓存用户信息 - 成功")
    void cacheUser_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.cacheUser(testUser);

        verify(valueOperations).set(eq("user:1"), eq(testUser), eq(7200L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("删除用户登录信息 - 成功")
    void removeUserLogin_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("token:test.token.123"))).thenReturn(loginResponse);

        cacheService.removeUserLogin(testToken);

        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    @DisplayName("删除用户登录信息 - Token为null时跳过")
    void removeUserLogin_NullToken() {
        cacheService.removeUserLogin(null);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("删除用户缓存 - 成功")
    void removeUserCache_Success() {
        cacheService.removeUserCache(1L);

        verify(redisTemplate).delete("user:1");
    }

    @Test
    @DisplayName("检查Token是否有效 - 有效")
    void isTokenValid_Valid() {
        when(redisTemplate.hasKey("token:test.token.123")).thenReturn(true);

        boolean valid = cacheService.isTokenValid(testToken);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("检查Token是否有效 - 无效")
    void isTokenValid_Invalid() {
        when(redisTemplate.hasKey("token:test.token.123")).thenReturn(false);

        boolean valid = cacheService.isTokenValid(testToken);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("根据用户ID获取Token - 成功")
    void getTokenByUserId_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:1:token")).thenReturn(testToken);

        String token = cacheService.getTokenByUserId(1L);

        assertThat(token).isEqualTo(testToken);
    }

    @Test
    @DisplayName("更新用户Token - 成功")
    void updateUserToken_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheService.updateUserToken(1L, "oldToken", "newToken");

        verify(redisTemplate).delete("token:oldToken");
        verify(valueOperations).set(eq("user:1:token"), eq("newToken"), eq(7200L), eq(TimeUnit.SECONDS));
    }
}