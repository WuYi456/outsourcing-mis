package com.tx.outsourcingmis.interceptor;

import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.service.CacheService;
import com.tx.outsourcingmis.utils.JwtUtils;
import com.tx.outsourcingmis.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * JWT 认证拦截器
 *
 * <p>拦截请求，验证 JWT Token 有效性，并将用户信息存入 ThreadLocal。
 * <p>执行流程：
 * <ol>
 *   <li>从请求头提取 Token</li>
 *   <li>验证 JWT 签名和有效期</li>
 *   <li>验证 Redis 中是否存在该 Token（防止登出后继续使用）</li>
 *   <li>从 Redis 缓存或 JWT 解析用户信息</li>
 *   <li>将用户信息存入 ThreadLocal（供后续业务使用）</li>
 * </ol>
 * <p>无 Token 的请求直接放行，由 PermissionInterceptor 判断是否需要登录。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final CacheService cacheService;

    /** Token 前缀 */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求前置处理：验证 Token 并设置用户上下文
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return true 继续执行，false 中断请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return true;
        }

        // 验证 JWT 签名和有效期
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new RuntimeException("Token无效或已过期");
        }

        // 验证 Redis 中是否存在（防止登出后继续使用）
        if (!cacheService.isTokenValid(token)) {
            log.warn("Token已失效: {}", maskToken(token));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new RuntimeException("Token已失效，请重新登录");
        }

        // 获取用户信息（优先从 Redis 缓存，减少 JWT 解析开销）
        LoginResponse loginInfo = cacheService.getLoginResponseByToken(token);
        if (loginInfo != null) {
            setUserContext(loginInfo.getUserId(), loginInfo.getUsername(),
                    loginInfo.getRole(), loginInfo.getPermissions(), token);
        } else {
            // 缓存未命中，从 JWT 解析（兜底）
            setUserContext(
                    jwtUtils.getUserIdFromToken(token),
                    jwtUtils.getUsernameFromToken(token),
                    jwtUtils.getRoleFromToken(token),
                    jwtUtils.getPermissionsFromToken(token),
                    token
            );
        }
        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    /**
     * 从请求头提取 Token
     *
     * @param request HTTP 请求
     * @return Token，不存在时返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith(TOKEN_PREFIX)) {
            return bearer.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 设置用户上下文到 ThreadLocal
     *
     * @param userId      用户 ID
     * @param username    用户名
     * @param role        角色
     * @param permissions 权限列表
     * @param token       JWT Token
     */
    private void setUserContext(Long userId, String username, String role,
                                List<String> permissions, String token) {
        UserContextHolder.setCurrentUserId(userId);
        UserContextHolder.setCurrentUsername(username);
        UserContextHolder.setCurrentRole(role);
        UserContextHolder.setCurrentPermissions(permissions);
        UserContextHolder.setCurrentToken(token);
    }

    /**
     * 脱敏 Token（仅显示前 10 位）
     *
     * @param token 原始 Token
     * @return 脱敏后的 Token
     */
    private String maskToken(String token) {
        if (token == null) {
            return null;
        }
        int length = Math.min(10, token.length());
        return token.substring(0, length) + "...";
    }
}