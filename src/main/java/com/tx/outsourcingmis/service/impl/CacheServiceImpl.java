package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务实现类
 *
 * <p>缓存策略：
 * <ul>
 *   <li>Token → LoginResponse：用于快速获取用户登录信息</li>
 *   <li>User ID → Token：用于登出时清理</li>
 *   <li>User ID → User：用于减少数据库查询</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;

    /** 用户缓存 Key 前缀 */
    @Value("${redis.cache.user-prefix:user:}")
    private String userPrefix;

    /** Token 缓存 Key 前缀 */
    @Value("${redis.cache.token-prefix:token:}")
    private String tokenPrefix;

    /** 会话缓存过期时间（秒），默认 2 小时 */
    @Value("${redis.cache.session-ttl:7200}")
    private Long sessionTtl;

    /**
     * 获取用户缓存 Key
     *
     * @param userId 用户 ID
     * @return Redis Key
     */
    private String userKey(Long userId) {
        return userPrefix + userId;
    }

    /**
     * 获取 Token 缓存 Key
     *
     * @param token JWT Token
     * @return Redis Key
     */
    private String tokenKey(String token) {
        return tokenPrefix + token;
    }

    /**
     * 获取用户 Token 映射 Key
     *
     * @param userId 用户 ID
     * @return Redis Key
     */
    private String userTokenKey(Long userId) {
        return userPrefix + userId + ":token";
    }

    @Override
    public void cacheUserLogin(String token, LoginResponse loginResponse, Long userId) {
        redisTemplate.opsForValue().set(tokenKey(token), loginResponse, sessionTtl, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(userTokenKey(userId), token, sessionTtl, TimeUnit.SECONDS);
    }

    @Override
    public LoginResponse getLoginResponseByToken(String token) {
        String key = tokenKey(token);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof LoginResponse response) {
            // 访问时刷新过期时间（续期）
            redisTemplate.expire(key, sessionTtl, TimeUnit.SECONDS);
            return response;
        }
        return null;
    }

    @Override
    public User getUserById(Long userId) {
        String key = userKey(userId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof User user) {
            return user;
        }
        // 缓存未命中，查询数据库并缓存
        User user = userMapper.selectById(userId);
        if (user != null) {
            cacheUser(user);
        }
        return user;
    }

    @Override
    public void cacheUser(User user) {
        redisTemplate.opsForValue().set(userKey(user.getId()), user, sessionTtl, TimeUnit.SECONDS);
    }

    @Override
    public void removeUserLogin(String token) {
        if (token == null) {
            return;
        }
        LoginResponse response = getLoginResponseByToken(token);
        if (response != null) {
            redisTemplate.delete(userTokenKey(response.getUserId()));
        }
        redisTemplate.delete(tokenKey(token));
    }

    @Override
    public void removeUserCache(Long userId) {
        redisTemplate.delete(userKey(userId));
    }

    @Override
    public boolean isTokenValid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey(token)));
    }

    @Override
    public String getTokenByUserId(Long userId) {
        Object token = redisTemplate.opsForValue().get(userTokenKey(userId));
        return token instanceof String ? (String) token : null;
    }

    @Override
    public void updateUserToken(Long userId, String oldToken, String newToken) {
        if (oldToken != null) {
            redisTemplate.delete(tokenKey(oldToken));
        }
        redisTemplate.opsForValue().set(userTokenKey(userId), newToken, sessionTtl, TimeUnit.SECONDS);
    }
}