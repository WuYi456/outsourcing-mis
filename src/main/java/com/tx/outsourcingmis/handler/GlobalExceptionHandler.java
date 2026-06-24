package com.tx.outsourcingmis.handler;

import com.tx.outsourcingmis.common.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>统一处理系统异常，返回规范的 ResultVO 响应。
 * <p>处理异常类型：
 * <ul>
 *   <li>RuntimeException —— 业务异常，根据消息内容判断返回 401/403/500</li>
 *   <li>MethodArgumentNotValidException —— 参数校验异常（@Valid 校验失败）</li>
 *   <li>BindException —— 参数绑定异常</li>
 *   <li>Exception —— 未捕获的其它异常，返回 500</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常（业务异常）
     *
     * <p>根据异常消息内容判断错误类型：
     * <ul>
     *   <li>包含 "Token" → 401 未授权</li>
     *   <li>包含 "权限" 或 "无权限" → 403 禁止访问</li>
     *   <li>其它 → 500 业务错误</li>
     * </ul>
     *
     * @param e 运行时异常
     * @return ResultVO
     */
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<Void> handleRuntimeException(RuntimeException e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("Token")) {
            return ResultVO.error(401, msg);
        }
        if (msg != null && (msg.contains("权限") || msg.contains("无权限"))) {
            return ResultVO.error(403, msg);
        }
        log.warn("业务异常: {}", msg);
        return ResultVO.error(msg);
    }

    /**
     * 处理参数校验异常（@Valid 校验失败）
     *
     * @param e 参数校验异常
     * @return ResultVO（状态码 400）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", msg);
        return ResultVO.error(400, msg);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e 参数绑定异常
     * @return ResultVO（状态码 400）
     */
    @ExceptionHandler(BindException.class)
    public ResultVO<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", msg);
        return ResultVO.error(400, msg);
    }

    /**
     * 处理系统异常（兜底）
     *
     * @param e 系统异常
     * @return ResultVO（状态码 500）
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultVO.error(500, "系统内部错误");
    }
}