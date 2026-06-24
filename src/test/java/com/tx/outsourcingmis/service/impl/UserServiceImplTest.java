// src/test/java/com/tx/outsourcingmis/service/impl/UserServiceImplTest.java
package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.dto.LoginRequest;
import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.RolePermissionMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.mapper.UserRoleMapper;
import com.tx.outsourcingmis.service.CacheService;
import com.tx.outsourcingmis.utils.JwtUtils;
import com.tx.outsourcingmis.utils.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword123");
        testUser.setRole("EMPLOYEE");
        testUser.setStatus(1);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void register_Success() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPwd");
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(userRoleMapper.getRoleIdByRoleName("EMPLOYEE")).thenReturn(1L);

        userService.register(newUser);

        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在，应抛出异常")
    void register_UsernameExists_ThrowsException() {
        User newUser = new User();
        newUser.setUsername("existingUser");

        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> userService.register(newUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("用户名已存在");

        verify(userMapper, never()).insert(any());
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void login_Success() {
        List<String> permissions = Arrays.asList("user:view", "job:submit");
        String expectedToken = "jwt.token.123";

        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("123456", "encodedPassword123")).thenReturn(true);
        when(userRoleMapper.getRoleByUserId(1L)).thenReturn("EMPLOYEE");
        when(rolePermissionMapper.getPermissionsByRole("EMPLOYEE")).thenReturn(permissions);
        when(jwtUtils.generateToken(1L, "testuser", "EMPLOYEE", permissions)).thenReturn(expectedToken);

        LoginResponse response = userService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(cacheService).cacheUser(any(User.class));
        verify(cacheService).cacheUserLogin(eq(expectedToken), any(LoginResponse.class), eq(1L));
    }

    @Test
    @DisplayName("用户登录 - 用户名不存在，应抛出异常")
    void login_UserNotFound_ThrowsException() {
        when(userMapper.selectByUsername("testuser")).thenReturn(null);

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("用户名不存在");
    }

    @Test
    @DisplayName("用户登录 - 账号被禁用，应抛出异常")
    void login_UserDisabled_ThrowsException() {
        testUser.setStatus(0);
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("账号已被禁用");
    }

    @Test
    @DisplayName("用户登录 - 密码错误，应抛出异常")
    void login_WrongPassword_ThrowsException() {
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("123456", "encodedPassword123")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("密码错误");
    }

    @Test
    @DisplayName("用户登出 - 成功")
    void logout_Success() {
        String mockToken = "mock.jwt.token";
        UserContextHolder.setCurrentUserId(1L);
        UserContextHolder.setCurrentUsername("testuser");
        UserContextHolder.setCurrentToken(mockToken);

        userService.logout();

        verify(cacheService).removeUserLogin(mockToken);
        assertThat(UserContextHolder.getCurrentUserId()).isNull();
    }

    @Test
    @DisplayName("获取当前用户 - 成功")
    void getCurrentUser_Success() {
        UserContextHolder.setCurrentUserId(1L);
        when(cacheService.getUserById(1L)).thenReturn(testUser);

        User result = userService.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }
}