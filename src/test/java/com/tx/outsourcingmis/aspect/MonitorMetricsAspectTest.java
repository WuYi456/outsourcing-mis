// src/test/java/com/tx/outsourcingmis/aspect/MonitorMetricsAspectTest.java
package com.tx.outsourcingmis.aspect;

import com.tx.outsourcingmis.config.MetricsConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("监控指标切面单元测试")
class MonitorMetricsAspectTest {

    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private MonitorMetricsAspect monitorMetricsAspect;

    static class TestController {
        public void testMethod() {}
    }

    static class TestMapper {
        public void selectById() {}
        public void slowQuery() {}
        public void failingQuery() {}
    }

    @Test
    @DisplayName("监控Controller - 正常执行")
    void monitorController_Success() throws Throwable {
        // 只设置会被实际调用的方法
        when(joinPoint.proceed()).thenReturn("success");
        // getTarget() 和 getSignature() 会被调用，但 toShortString() 可能不会
        when(joinPoint.getTarget()).thenReturn(new TestController());
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        monitorMetricsAspect.monitorController(joinPoint);

        verify(metricsConfig, atLeastOnce()).recordApiDuration(anyString(), anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    @DisplayName("监控Controller - 执行异常")
    void monitorController_Exception() throws Throwable {
        RuntimeException testException = new RuntimeException("测试异常");
        when(joinPoint.proceed()).thenThrow(testException);
        when(joinPoint.getTarget()).thenReturn(new TestController());
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        try {
            monitorMetricsAspect.monitorController(joinPoint);
        } catch (RuntimeException e) {
            assert e.getMessage().equals("测试异常");
        }

        verify(metricsConfig).recordBusinessEvent(eq("api_error"), anyString(), isNull());
    }

    @Test
    @DisplayName("监控Service - 正常执行")
    void monitorService_Success() throws Throwable {
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        monitorMetricsAspect.monitorService(joinPoint);

        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("监控Service - 慢调用告警")
    void monitorService_SlowCall() throws Throwable {
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(1500);
            return "success";
        });
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        monitorMetricsAspect.monitorService(joinPoint);

        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("监控Mapper - 正常执行")
    void monitorMapper_Success() throws Throwable {
        when(joinPoint.proceed()).thenReturn("success");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn((Class) TestMapper.class);
        when(methodSignature.getName()).thenReturn("selectById");

        monitorMetricsAspect.monitorMapper(joinPoint);

        verify(metricsConfig, atLeastOnce()).recordDbQueryDuration(anyString(), anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    @DisplayName("监控Mapper - 慢查询告警")
    void monitorMapper_SlowQuery() throws Throwable {
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(1200);
            return "success";
        });
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn((Class) TestMapper.class);
        when(methodSignature.getName()).thenReturn("slowQuery");

        monitorMetricsAspect.monitorMapper(joinPoint);

        verify(metricsConfig).recordSlowQuery(anyString(), anyLong());
    }

    @Test
    @DisplayName("监控Mapper - 执行异常")
    void monitorMapper_Exception() throws Throwable {
        RuntimeException testException = new RuntimeException("SQL异常");
        when(joinPoint.proceed()).thenThrow(testException);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn((Class) TestMapper.class);
        when(methodSignature.getName()).thenReturn("failingQuery");

        try {
            monitorMetricsAspect.monitorMapper(joinPoint);
        } catch (RuntimeException e) {
            assert e.getMessage().equals("SQL异常");
        }

        verify(joinPoint, times(1)).proceed();
    }
}