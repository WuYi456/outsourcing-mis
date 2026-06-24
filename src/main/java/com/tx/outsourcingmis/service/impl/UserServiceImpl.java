package com.tx.outsourcingmis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tx.outsourcingmis.dto.LoginRequest;
import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.RolePermissionMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.mapper.UserRoleMapper;
import com.tx.outsourcingmis.service.CacheService;
import com.tx.outsourcingmis.service.UserService;
import com.tx.outsourcingmis.utils.JwtUtils;
import com.tx.outsourcingmis.utils.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现类
 *
 * <p>核心功能：
 * <ul>
 *   <li>用户注册：密码加密、默认角色分配</li>
 *   <li>用户登录：密码验证、JWT 生成、Redis 缓存</li>
 *   <li>用户登出：清除缓存</li>
 *   <li>用户信息查询：支持缓存</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    @Override
    public void register(User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 默认角色：未指定时设为 EMPLOYEE
        String role = user.getRole();
        if (!StringUtils.hasText(role)) {
            role = "EMPLOYEE";
            user.setRole(role);
        }

        // 默认状态：启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 保存用户
        userMapper.insert(user);

        // 分配角色
        Long roleId = userRoleMapper.getRoleIdByRoleName(role);
        if (roleId != null) {
            userRoleMapper.assignRoleToUser(user.getId(), roleId);
        }

        log.info("用户注册成功: {}, 角色: {}", user.getUsername(), role);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 验证用户名和密码
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 查询角色
        String role = userRoleMapper.getRoleByUserId(user.getId());
        if (role == null) {
            role = "EMPLOYEE";
        }

        // 查询权限列表
        List<String> permissions = rolePermissionMapper.getPermissionsByRole(role);

        // 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), role, permissions);

        // 构建登录响应
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(role)
                .permissions(permissions)
                .build();

        // 缓存用户信息和登录会话
        cacheService.cacheUser(user);
        cacheService.cacheUserLogin(token, response, user.getId());

        log.info("用户登录成功: {}, 角色: {}", user.getUsername(), role);
        return response;
    }

    @Override
    public void logout() {
        String token = UserContextHolder.getCurrentToken();
        String username = UserContextHolder.getCurrentUsername();

        // 清除 Redis 缓存
        cacheService.removeUserLogin(token);
        log.info("用户登出: {}", username);

        // 清理 ThreadLocal
        UserContextHolder.clear();
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未获取到当前用户信息");
        }
        return cacheService.getUserById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            cacheService.cacheUser(user);
        }
        return user;
    }
}