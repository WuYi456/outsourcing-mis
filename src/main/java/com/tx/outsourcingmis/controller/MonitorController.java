package com.tx.outsourcingmis.controller;

import com.tx.outsourcingmis.common.ResultVO;
import com.tx.outsourcingmis.config.RabbitMQConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 系统健康检查控制器
 *
 * <p>提供中间件健康状态监控接口：
 * <ul>
 *   <li>Redis 连接状态检查</li>
 *   <li>RabbitMQ 连接状态检查</li>
 *   <li>RabbitMQ 队列大小查询</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Tag(name = "监控模块", description = "系统健康检查")
public class MonitorController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    /** Redis 健康检查 Key */
    private static final String HEALTH_CHECK_KEY = "health_check";

    /** Redis 健康检查 Key 过期时间 */
    private static final Duration HEALTH_CHECK_TTL = Duration.ofSeconds(10);

    /**
     * Redis 健康检查
     *
     * <p>向 Redis 写入测试数据并读取，验证连接是否正常。
     *
     * @return true 表示连接正常，false 表示异常
     */
    @GetMapping("/redis/health")
    @Operation(summary = "Redis健康检查")
    public ResultVO<Boolean> checkRedisHealth() {
        try {
            redisTemplate.opsForValue().set(HEALTH_CHECK_KEY, "ok", HEALTH_CHECK_TTL);
            String result = (String) redisTemplate.opsForValue().get(HEALTH_CHECK_KEY);
            return ResultVO.success("ok".equals(result));
        } catch (Exception e) {
            log.warn("Redis 健康检查失败: {}", e.getMessage());
            return ResultVO.error(500, "Redis连接失败");
        }
    }

    /**
     * 查询 RabbitMQ 审批队列大小
     *
     * <p>获取 {@link RabbitMQConfig#APPROVAL_QUEUE} 队列中待消费的消息数量。
     *
     * @return 队列消息数量
     */
    @GetMapping("/rabbitmq/queue-size")
    @Operation(summary = "查看RabbitMQ队列大小")
    public ResultVO<Integer> getRabbitMQQueueSize() {
        try {
            Integer size = rabbitTemplate.execute(
                    channel -> channel.queueDeclarePassive(RabbitMQConfig.APPROVAL_QUEUE).getMessageCount()
            );
            return ResultVO.success(size);
        } catch (Exception e) {
            log.warn("获取 RabbitMQ 队列大小失败: {}", e.getMessage());
            return ResultVO.error("获取队列大小失败");
        }
    }

    /**
     * RabbitMQ 健康检查
     *
     * <p>尝试声明队列，验证 RabbitMQ 连接是否正常。
     *
     * @return true 表示连接正常，false 表示异常
     */
    @GetMapping("/rabbitmq/health")
    @Operation(summary = "RabbitMQ健康检查")
    public ResultVO<Boolean> checkRabbitMQHealth() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive(RabbitMQConfig.APPROVAL_QUEUE);
                return true;
            });
            return ResultVO.success(true);
        } catch (Exception e) {
            log.warn("RabbitMQ 健康检查失败: {}", e.getMessage());
            return ResultVO.error(500, "RabbitMQ连接失败");
        }
    }
}