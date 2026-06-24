// src/test/java/com/tx/outsourcingmis/interceptor/PermissionInterceptorTest.java
package com.tx.outsourcingmis.interceptor;

import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("权限拦截器测试")
class PermissionInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private PermissionInterceptor permissionInterceptor;

    @BeforeEach
    void setUp() {
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("非Controller方法 - 放行")
    void preHandle_NotHandlerMethod_ReturnsTrue() throws Exception {
        Object handler = "not a handler method";

        boolean result = permissionInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("没有权限注解 - 放行")
    void preHandle_NoRequirePermission_ReturnsTrue() throws Exception {
        // 创建没有权限注解的 HandlerMethod
        Method method = NoAnnotationController.class.getMethod("testMethod");
        HandlerMethod handlerMethod = new HandlerMethod(new NoAnnotationController(), method);

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("有权限注解但用户未登录 - 抛出异常")
    void preHandle_RequirePermission_NotLoggedIn_ThrowsException() throws Exception {
        Method method = TestControllerWithPermission.class.getMethod("testMethod");
        HandlerMethod handlerMethod = new HandlerMethod(new TestControllerWithPermission(), method);

        UserContextHolder.setCurrentRole(null);
        UserContextHolder.setCurrentPermissions(null);

        assertThatThrownBy(() -> permissionInterceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("未登录");
    }

    @Test
    @DisplayName("有权限注解且用户有权限 - 放行")
    void preHandle_HasPermission_ReturnsTrue() throws Exception {
        Method method = TestControllerWithPermission.class.getMethod("testMethod");
        HandlerMethod handlerMethod = new HandlerMethod(new TestControllerWithPermission(), method);

        UserContextHolder.setCurrentRole("ADMIN");
        UserContextHolder.setCurrentPermissions(Arrays.asList("test:view", "job:submit"));

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("有权限注解但用户无权限 - 抛出异常")
    void preHandle_NoPermission_ThrowsException() throws Exception {
        Method method = TestControllerWithAdminPermission.class.getMethod("testMethod");
        HandlerMethod handlerMethod = new HandlerMethod(new TestControllerWithAdminPermission(), method);

        UserContextHolder.setCurrentRole("EMPLOYEE");
        UserContextHolder.setCurrentPermissions(Arrays.asList("user:view", "job:submit"));

        assertThatThrownBy(() -> permissionInterceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权限访问");
    }

    // ========== 测试用的内部类 ==========

    // 没有权限注解的Controller
    static class NoAnnotationController {
        public void testMethod() {}
    }

    // 带权限注解的Controller
    @RequirePermission("test:view")
    static class TestControllerWithPermission {
        public void testMethod() {}
    }

    // 带管理员权限注解的Controller
    @RequirePermission({"admin:delete", "admin:create"})
    static class TestControllerWithAdminPermission {
        public void testMethod() {}
    }
}