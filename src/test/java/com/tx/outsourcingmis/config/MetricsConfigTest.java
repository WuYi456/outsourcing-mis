// src/test/java/com/tx/outsourcingmis/config/MetricsConfigTest.java
package com.tx.outsourcingmis.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("监控指标配置测试")
class MetricsConfigTest {

    private MetricsConfig metricsConfig;

    @BeforeEach
    void setUp() {
        metricsConfig = new MetricsConfig(new SimpleMeterRegistry());
        metricsConfig.init();
    }

    @Test
    @DisplayName("记录API耗时")
    void recordApiDuration() {
        metricsConfig.recordApiDuration("testApi", 100, TimeUnit.MILLISECONDS);
        metricsConfig.recordApiDuration("testApi", 500, TimeUnit.MILLISECONDS);
        // 不抛异常即成功
        assertThat(metricsConfig).isNotNull();
    }

    @Test
    @DisplayName("记录数据库查询耗时")
    void recordDbQueryDuration() {
        metricsConfig.recordDbQueryDuration("testQuery", 200, TimeUnit.MILLISECONDS);
        assertThat(metricsConfig).isNotNull();
    }

    @Test
    @DisplayName("记录Redis操作耗时")
    void recordRedisDuration() {
        metricsConfig.recordRedisDuration("get", 50, TimeUnit.MILLISECONDS);
        assertThat(metricsConfig).isNotNull();
    }

    @Test
    @DisplayName("记录MQ发送耗时")
    void recordMqSendDuration() {
        metricsConfig.recordMqSendDuration("testQueue", 150, TimeUnit.MILLISECONDS);
        assertThat(metricsConfig).isNotNull();
    }

    @Test
    @DisplayName("记录业务事件")
    void recordBusinessEvent() {
        metricsConfig.recordBusinessEvent("login", "success", "testuser");
        metricsConfig.recordBusinessEvent("login", "failed", null);
        assertThat(metricsConfig).isNotNull();
    }

    @Test
    @DisplayName("记录慢查询")
    void recordSlowQuery() {
        metricsConfig.recordSlowQuery("SELECT * FROM user", 1500);
        assertThat(metricsConfig).isNotNull();
    }
}