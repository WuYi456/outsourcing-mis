package com.tx.outsourcingmis.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在 Controller 方法或类上，用于声明访问所需权限
 *
 * 使用示例：
 * - 单个权限：@RequirePermission("user:view")
 * - 多个权限（满足其一即可）：@RequirePermission({"user:view", "user:edit"})
 *
 * 配合 PermissionInterceptor 拦截器生效
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 所需的权限标识列表
     * 用户拥有其中任意一个即可放行
     */
    String[] value() default {};
}