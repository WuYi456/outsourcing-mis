package com.tx.outsourcingmis.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tx.outsourcingmis.es.document.OperationLogDocument;
import com.tx.outsourcingmis.service.OperationLogService;
import com.tx.outsourcingmis.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 操作日志切面
 *
 * <p>功能：拦截所有 Controller 请求，自动记录操作日志到 Elasticsearch
 * <p>记录内容包括：操作用户、请求IP、请求参数、执行结果、耗时、异常信息等
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    /** 请求参数最大存储长度 */
    private static final int PARAMS_MAX_LENGTH = 1000;

    /** 执行结果最大存储长度 */
    private static final int RESULT_MAX_LENGTH = 2000;

    /** 错误信息最大存储长度 */
    private static final int ERROR_MAX_LENGTH = 500;

    /** IP 头字段列表（按优先级排列） */
    private static final String[] IP_HEADERS = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP"};

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller)")
    public void controllerPointcut() {}

    /**
     * 环绕通知：拦截 Controller 方法，记录操作日志
     *
     * <p>无论方法执行成功还是抛出异常，都会记录日志并异步保存到 ES
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("controllerPointcut()")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 构建日志对象
        OperationLogDocument.OperationLogDocumentBuilder logBuilder = OperationLogDocument.builder()
                .id(UUID.randomUUID().toString())
                .createTime(LocalDateTime.now());

        fillUserInfo(logBuilder);
        fillRequestInfo(logBuilder);

        // 方法信息：类名.方法名
        logBuilder.operation(signature.getDeclaringType().getSimpleName() + "." + signature.getName());

        // 请求参数（序列化后截断）
        logBuilder.params(truncateString(getParamsString(joinPoint.getArgs()), PARAMS_MAX_LENGTH));

        try {
            Object result = joinPoint.proceed();
            logBuilder.result(truncateString(getResultString(result), RESULT_MAX_LENGTH));
            logBuilder.success(true);
            return result;
        } catch (Exception e) {
            logBuilder.success(false);
            logBuilder.errorMsg(truncateString(e.getMessage(), ERROR_MAX_LENGTH));
            log.error("操作日志记录 - 请求执行异常: {}", e.getMessage(), e);
            throw e;
        } finally {
            logBuilder.duration(System.currentTimeMillis() - startTime);
            operationLogService.saveLogAsync(logBuilder.build());
        }
    }

    /**
     * 填充用户信息到日志对象
     *
     * @param builder 日志构建器
     */
    private void fillUserInfo(OperationLogDocument.OperationLogDocumentBuilder builder) {
        Long userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        if (userId != null) {
            builder.userId(userId);
        }
        if (username != null) {
            builder.username(username);
        }
    }

    /**
     * 填充请求信息到日志对象（IP、请求方法 + URI）
     *
     * @param builder 日志构建器
     */
    private void fillRequestInfo(OperationLogDocument.OperationLogDocumentBuilder builder) {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            builder.ip(getClientIp(request));
            builder.method(request.getMethod() + " " + request.getRequestURI());
        }
    }

    /**
     * 获取当前请求的 HttpServletRequest 对象
     *
     * @return HttpServletRequest，获取失败时返回 null
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("获取 HttpServletRequest 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取客户端真实 IP
     *
     * <p>按优先级依次尝试：X-Forwarded-For → Proxy-Client-IP → WL-Proxy-Client-IP → RemoteAddr
     *
     * @param request HttpServletRequest
     * @return 客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个 IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 将请求参数序列化为 JSON 字符串
     *
     * @param args 参数数组
     * @return JSON 字符串，序列化失败时返回错误描述
     */
    private String getParamsString(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            log.warn("请求参数序列化失败: {}", e.getMessage());
            return "参数序列化失败: " + e.getMessage();
        }
    }

    /**
     * 将方法执行结果序列化为 JSON 字符串
     *
     * @param result 方法返回值
     * @return JSON 字符串，序列化失败时返回 toString() 结果
     */
    private String getResultString(Object result) {
        if (result == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.warn("执行结果序列化失败: {}", e.getMessage());
            return result.toString();
        }
    }

    /**
     * 截断字符串，防止存储过大
     *
     * @param str 原字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串，超过最大长度时末尾追加 "...(truncated)"
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }
}