package com.tx.outsourcingmis.aspect;

import com.tx.outsourcingmis.config.MetricsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 系统监控切面
 * 功能：自动采集 Controller/Service/Mapper 层方法执行耗时，上报 Prometheus
 *
 * <p>告警阈值：
 * <ul>
 *   <li>Controller：> 500ms 打印慢API告警</li>
 *   <li>Service：> 1000ms 打印慢调用告警</li>
 *   <li>Mapper：> 1000ms 记录慢SQL并告警</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MonitorMetricsAspect {

    private final MetricsConfig metricsConfig;

    /** Controller 层切点表达式 */
    private static final String CONTROLLER_POINTCUT =
            "@within(org.springframework.web.bind.annotation.RestController) || " +
                    "@within(org.springframework.stereotype.Controller)";

    /** Service 层切点表达式 */
    private static final String SERVICE_POINTCUT = "@within(org.springframework.stereotype.Service)";

    /** Mapper 层切点表达式 */
    private static final String MAPPER_POINTCUT = "@within(org.apache.ibatis.annotations.Mapper)";

    /** API 慢调用告警阈值（毫秒） */
    private static final long SLOW_API_THRESHOLD_MS = 500;

    /** Service 慢调用告警阈值（毫秒） */
    private static final long SLOW_SERVICE_THRESHOLD_MS = 1000;

    /** SQL 慢查询告警阈值（毫秒） */
    private static final long SLOW_SQL_THRESHOLD_MS = 1000;

    @Pointcut(CONTROLLER_POINTCUT)
    public void controllerMethods() {}

    @Pointcut(SERVICE_POINTCUT)
    public void serviceMethods() {}

    @Pointcut(MAPPER_POINTCUT)
    public void mapperMethods() {}

    /**
     * 监控 Controller 层方法执行耗时
     *
     * <p>超过 500ms 时记录慢API告警日志，异常时记录业务事件
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("controllerMethods()")
    public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String apiName = className + "." + methodName;

        long startTime = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long duration = System.nanoTime() - startTime;
            long durationMs = TimeUnit.NANOSECONDS.toMillis(duration);

            metricsConfig.recordApiDuration(apiName, duration, TimeUnit.NANOSECONDS);

            if (durationMs > SLOW_API_THRESHOLD_MS) {
                log.warn("慢API告警: {} 耗时 {}ms", apiName, durationMs);
            }
            return result;
        } catch (Exception e) {
            long duration = System.nanoTime() - startTime;
            metricsConfig.recordApiDuration(apiName, duration, TimeUnit.NANOSECONDS);
            metricsConfig.recordBusinessEvent("api_error", e.getClass().getSimpleName(), null);
            log.error("API执行异常: {}, 耗时 {}ms", apiName, TimeUnit.NANOSECONDS.toMillis(duration), e);
            throw e;
        }
    }

    /**
     * 监控 Service 层方法执行耗时
     *
     * <p>超过 1000ms 时记录慢调用告警日志
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("serviceMethods()")
    public Object monitorService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.nanoTime();

        Object result = joinPoint.proceed();
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        if (duration > SLOW_SERVICE_THRESHOLD_MS) {
            log.warn("慢Service调用: {} 耗时 {}ms", methodName, duration);
        }
        return result;
    }

    /**
     * 监控 Mapper 层方法执行耗时（数据库查询）
     *
     * <p>超过 1000ms 时记录慢SQL告警日志并上报指标
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("mapperMethods()")
    public Object monitorMapper(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String sqlMethod = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        long startTime = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long duration = System.nanoTime() - startTime;
            long durationMs = TimeUnit.NANOSECONDS.toMillis(duration);

            metricsConfig.recordDbQueryDuration(sqlMethod, duration, TimeUnit.NANOSECONDS);

            if (durationMs > SLOW_SQL_THRESHOLD_MS) {
                metricsConfig.recordSlowQuery(sqlMethod, durationMs);
                log.warn("慢SQL告警: {} 耗时 {}ms", sqlMethod, durationMs);
            }
            return result;
        } catch (Exception e) {
            log.error("SQL执行异常: {}, 耗时 {}ms", sqlMethod, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime), e);
            throw e;
        }
    }
}