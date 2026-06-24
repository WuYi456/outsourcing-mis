package com.tx.outsourcingmis.service;

import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;

/**
 * Redis 缓存服务接口
 *
 * <p>提供用户登录会话和用户信息的缓存管理。
 */
public interface CacheService {

    /**
     * 缓存用户登录信息
     *
     * @param token JWT Token
     * @param loginResponse 登录响应
     * @param userId 用户 ID
     */
    void cacheUserLogin(String token, LoginResponse loginResponse, Long userId);

    /**
     * 根据 Token 获取登录信息
     *
     * @param token JWT Token
     * @return 登录响应，不存在时返回 null
     */
    LoginResponse getLoginResponseByToken(String token);

    /**
     * 根据用户 ID 获取用户信息（带缓存）
     *
     * @param userId 用户 ID
     * @return 用户信息，不存在时返回 null
     */
    User getUserById(Long userId);

    /**
     * 缓存用户信息
     *
     * @param user 用户信息
     */
    void cacheUser(User user);

    /**
     * 删除用户登录信息（登出时调用）
     *
     * @param token JWT Token
     */
    void removeUserLogin(String token);

    /**
     * 删除用户缓存
     *
     * @param userId 用户 ID
     */
    void removeUserCache(Long userId);

    /**
     * 检查 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    boolean isTokenValid(String token);

    /**
     * 获取用户当前有效的 Token
     *
     * @param userId 用户 ID
     * @return Token，不存在时返回 null
     */
    String getTokenByUserId(Long userId);

    /**
     * 更新用户 Token（重新登录时）
     *
     * @param userId 用户 ID
     * @param oldToken 旧 Token
     * @param newToken 新 Token
     */
    void updateUserToken(Long userId, String oldToken, String newToken);
}