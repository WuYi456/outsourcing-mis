package com.tx.outsourcingmis.utils;

import java.util.List;

/**
 * 用户上下文持有者
 *
 * <p>基于 ThreadLocal 存储当前请求的用户信息，包括用户 ID、用户名、角色、权限列表和 Token。
 * <p>在请求开始时由 JwtInterceptor 设置，请求结束后由 afterCompletion 清理，防止内存泄漏。
 *
 * <p>使用示例：
 * <pre>
 * // 获取当前用户 ID
 * Long userId = UserContextHolder.getCurrentUserId();
 *
 * // 获取当前用户名
 * String username = UserContextHolder.getCurrentUsername();
 * </pre>
 */
public class UserContextHolder {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> CURRENT_PERMISSIONS = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TOKEN = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setCurrentUsername(String username) {
        CURRENT_USERNAME.set(username);
    }

    public static String getCurrentUsername() {
        return CURRENT_USERNAME.get();
    }

    public static void setCurrentRole(String role) {
        CURRENT_ROLE.set(role);
    }

    public static String getCurrentRole() {
        return CURRENT_ROLE.get();
    }

    public static void setCurrentPermissions(List<String> permissions) {
        CURRENT_PERMISSIONS.set(permissions);
    }

    public static List<String> getCurrentPermissions() {
        return CURRENT_PERMISSIONS.get();
    }

    public static void setCurrentToken(String token) {
        CURRENT_TOKEN.set(token);
    }

    public static String getCurrentToken() {
        return CURRENT_TOKEN.get();
    }

    /**
     * 清除所有 ThreadLocal 变量
     *
     * <p>必须在每次请求结束后调用，避免内存泄漏。
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USERNAME.remove();
        CURRENT_ROLE.remove();
        CURRENT_PERMISSIONS.remove();
        CURRENT_TOKEN.remove();
    }
}