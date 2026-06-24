package com.tx.outsourcingmis.service;

import com.tx.outsourcingmis.dto.LoginRequest;
import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;

/**
 * 用户服务接口
 *
 * <p>提供用户注册、登录、登出和信息查询等认证相关业务操作。
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param user 用户信息（用户名、密码、真实姓名、邮箱、手机号等）
     */
    void register(User user);

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求（用户名、密码）
     * @return 登录响应（含 JWT Token、用户信息、角色、权限列表）
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户登出（清除 Redis 缓存中的登录状态）
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    User getCurrentUser();

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息，不存在时返回 null
     */
    User getUserByUsername(String username);
}