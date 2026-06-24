package com.tx.outsourcingmis.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Micrometer 监控指标配置
 *
 * <p>注册自定义 Prometheus 指标，用于监控 API 耗时、数据库查询耗时、Redis 操作耗时、MQ 发送耗时。
 * <p>指标数据通过 /actuator/prometheus 端点暴露，供 Prometheus 采集。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConfig {

    /** API 请求耗时指标名称 */
    public static final String API_REQUEST_DURATION = "api.request.duration";

    /** 数据库查询耗时指标名称 */
    public static final String DB_QUERY_DURATION = "db.query.duration";

    /** Redis 操作耗时指标名称 */
    public static final String REDIS_OP_DURATION = "redis.operation.duration";

    /** MQ 发送耗时指标名称 */
    public static final String MQ_SEND_DURATION = "mq.send.duration";

    /** 慢查询阈值（毫秒） */
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;

    /** SQL 日志最大显示长度 */
    private static final int SQL_MAX_DISPLAY_LENGTH = 100;

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 注册 API 耗时指标（带百分位数和 SLA 目标）
        timers.put(API_REQUEST_DURATION, buildTimer(API_REQUEST_DURATION, "API request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .sla(Duration.ofMillis(50), Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofSeconds(1))
                .register(meterRegistry));

        // 注册数据库查询耗时指标
        timers.put(DB_QUERY_DURATION, buildTimer(DB_QUERY_DURATION, "Database query duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));

        // 注册 Redis 操作耗时指标
        timers.put(REDIS_OP_DURATION, buildTimer(REDIS_OP_DURATION, "Redis operation duration")
                .register(meterRegistry));

        // 注册 MQ 发送耗时指标
        timers.put(MQ_SEND_DURATION, buildTimer(MQ_SEND_DURATION, "MQ send duration")
                .register(meterRegistry));

        log.info("Metrics 初始化完成，已注册 4 个自定义指标");
    }

    /**
     * 创建 Timer Builder
     *
     * @param name 指标名称
     * @param desc 指标描述
     * @return Timer.Builder
     */
    private Timer.Builder buildTimer(String name, String desc) {
        return Timer.builder(name).description(desc);
    }

    /**
     * 获取或创建 Timer
     *
     * <p>如果 Timer 不存在则自动创建并注册，保证指标可用性
     *
     * @param name 指标名称
     * @return Timer 实例
     */
    private Timer getTimer(String name) {
        Timer timer = timers.get(name);
        if (timer == null) {
            timer = Timer.builder(name).register(meterRegistry);
            timers.put(name, timer);
        }
        return timer;
    }

    /**
     * 记录 API 请求耗时
     *
     * @param apiName API 名称（类名.方法名）
     * @param duration 耗时
     * @param unit 时间单位
     */
    public void recordApiDuration(String apiName, long duration, TimeUnit unit) {
        getTimer(API_REQUEST_DURATION).record(duration, unit);
        meterRegistry.counter("api.request.total", "api", apiName).increment();
    }

    /**
     * 记录数据库查询耗时
     *
     * @param queryName 查询名称（Mapper.方法名）
     * @param duration 耗时
     * @param unit 时间单位
     */
    public void recordDbQueryDuration(String queryName, long duration, TimeUnit unit) {
        getTimer(DB_QUERY_DURATION).record(duration, unit);
        meterRegistry.counter("db.query.total", "query", queryName).increment();
    }

    /**
     * 记录 Redis 操作耗时
     *
     * @param operation 操作名称
     * @param duration 耗时
     * @param unit 时间单位
     */
    public void recordRedisDuration(String operation, long duration, TimeUnit unit) {
        getTimer(REDIS_OP_DURATION).record(duration, unit);
        meterRegistry.counter("redis.operation.total", "operation", operation).increment();
    }

    /**
     * 记录 MQ 发送耗时
     *
     * @param queueName 队列名称
     * @param duration 耗时
     * @param unit 时间单位
     */
    public void recordMqSendDuration(String queueName, long duration, TimeUnit unit) {
        getTimer(MQ_SEND_DURATION).record(duration, unit);
        meterRegistry.counter("mq.send.total", "queue", queueName).increment();
    }

    /**
     * 记录业务事件（自定义计数指标）
     *
     * @param eventName 事件名称
     * @param result 事件结果
     * @param username 操作用户名
     */
    public void recordBusinessEvent(String eventName, String result, String username) {
        meterRegistry.counter("business.event",
                "event", eventName,
                "result", result,
                "username", username != null ? username : "unknown").increment();
    }

    /**
     * 记录慢查询
     *
     * <p>超过 {@link #SLOW_QUERY_THRESHOLD_MS} 毫秒的查询会被记录到慢查询指标并打印告警日志
     *
     * @param sql SQL 语句（或方法名）
     * @param durationMs 耗时（毫秒）
     */
    public void recordSlowQuery(String sql, long durationMs) {
        if (durationMs > SLOW_QUERY_THRESHOLD_MS) {
            String shortSql = sql.length() > SQL_MAX_DISPLAY_LENGTH
                    ? sql.substring(0, SQL_MAX_DISPLAY_LENGTH) : sql;
            meterRegistry.counter("db.slow.query",
                    "sql", shortSql,
                    "duration_ms", String.valueOf(durationMs)).increment();
            log.warn("慢查询告警: {} 耗时 {}ms", shortSql, durationMs);
        }
    }
}