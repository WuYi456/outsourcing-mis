package com.tx.outsourcingmis.config;

import com.tx.outsourcingmis.interceptor.JwtInterceptor;
import com.tx.outsourcingmis.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置
 *
 * <p>注册拦截器：
 * <ul>
 *   <li>JwtInterceptor —— JWT 认证拦截器，验证 Token 有效性</li>
 *   <li>PermissionInterceptor —— 权限拦截器，校验用户权限</li>
 * </ul>
 * <p>拦截路径：/api/**，排除登录、注册、静态资源及接口文档路径。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    /** 不需要拦截的路径列表 */
    private static final List<String> EXCLUDE_PATHS = List.of(
            "/api/user/login",
            "/api/user/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/login",
            "/dashboard",
            "/user-management",
            "/applications",
            "/approval",
            "/performance",
            "/logs",
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**",
            "/favicon.ico"
    );

    /**
     * 注册拦截器
     *
     * <p>JwtInterceptor 先执行，验证 Token；
     * <p>PermissionInterceptor 后执行，校验权限。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 认证拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(EXCLUDE_PATHS);

        // 权限校验拦截器
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}