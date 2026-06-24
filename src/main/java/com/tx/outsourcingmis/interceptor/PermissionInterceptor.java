package com.tx.outsourcingmis.interceptor;

import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器
 *
 * <p>检查用户是否拥有访问目标接口所需的权限。
 * <p>执行流程：
 * <ol>
 *   <li>检查 Handler 是否有 @RequirePermission 注解</li>
 *   <li>获取注解中声明的所需权限列表</li>
 *   <li>检查用户是否已登录（角色不为空）</li>
 *   <li>检查用户权限列表是否包含任意一个所需权限</li>
 *   <li>权限匹配则放行，否则返回 403</li>
 * </ol>
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    /**
     * 请求前置处理：校验用户权限
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return true 权限通过，false 权限不足
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非 Controller 方法直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 获取权限注解
        RequirePermission annotation = getAnnotation(handlerMethod);
        if (annotation == null) {
            return true;
        }

        String[] required = annotation.value();
        if (required == null || required.length == 0) {
            return true;
        }

        // 检查用户是否已登录
        String role = UserContextHolder.getCurrentRole();
        if (role == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new RuntimeException("未登录");
        }

        // 获取用户权限列表
        List<String> userPermissions = UserContextHolder.getCurrentPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            log.warn("用户 {} 无任何权限", UserContextHolder.getCurrentUsername());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            throw new RuntimeException("无权限访问");
        }

        // 检查是否拥有任意一个所需权限
        List<String> requiredList = Arrays.asList(required);
        for (String permission : userPermissions) {
            if (requiredList.contains(permission)) {
                return true;
            }
        }

        // 权限不足
        log.warn("权限不足 - 用户: {}, 所需: [{}], 拥有: [{}]",
                UserContextHolder.getCurrentUsername(),
                String.join(", ", required),
                String.join(", ", userPermissions));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        throw new RuntimeException("无权限访问");
    }

    /**
     * 获取方法或类上的权限注解
     *
     * <p>优先获取方法级别注解，若不存在则获取类级别注解。
     *
     * @param handlerMethod 处理方法
     * @return RequirePermission 注解，不存在时返回 null
     */
    private RequirePermission getAnnotation(HandlerMethod handlerMethod) {
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        return annotation;
    }
}