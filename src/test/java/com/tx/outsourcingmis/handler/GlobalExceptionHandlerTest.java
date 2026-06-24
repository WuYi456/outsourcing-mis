package com.tx.outsourcingmis.handler;

import com.tx.outsourcingmis.common.ResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("处理运行时异常 - Token失效")
    void handleRuntimeException_TokenInvalid() {
        RuntimeException exception = new RuntimeException("Token无效或已过期");
        ResultVO<Void> result = exceptionHandler.handleRuntimeException(exception);
        assertThat(result.getCode()).isEqualTo(401);
        assertThat(result.getMsg()).contains("Token");
    }

    @Test
    @DisplayName("处理运行时异常 - 无权限")
    void handleRuntimeException_Forbidden() {
        RuntimeException exception = new RuntimeException("无权限访问");
        ResultVO<Void> result = exceptionHandler.handleRuntimeException(exception);
        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMsg()).contains("权限");
    }

    @Test
    @DisplayName("处理运行时异常 - 普通业务异常")
    void handleRuntimeException_General() {
        RuntimeException exception = new RuntimeException("用户不存在");
        ResultVO<Void> result = exceptionHandler.handleRuntimeException(exception);
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("用户不存在");
    }

    @Test
    @DisplayName("处理参数校验异常")
    void handleValidationException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);

        FieldError fieldError = new FieldError("object", "username", "用户名不能为空");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResultVO<Void> result = exceptionHandler.handleValidationException(exception);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).contains("用户名不能为空");
    }

    @Test
    @DisplayName("处理参数绑定异常")
    void handleBindException() {
        BindException exception = mock(BindException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);

        FieldError fieldError = new FieldError("object", "password", "密码不能为空");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResultVO<Void> result = exceptionHandler.handleBindException(exception);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).contains("密码不能为空");
    }

    @Test
    @DisplayName("处理系统异常")
    void handleException() {
        Exception exception = new Exception("系统内部错误");
        ResultVO<Void> result = exceptionHandler.handleException(exception);
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("系统内部错误");
    }
}