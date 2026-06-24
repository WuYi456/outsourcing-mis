package com.tx.outsourcingmis.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Elasticsearch 自定义健康检查
 *
 * <p>实现 Spring Boot Actuator 健康检查接口，用于监控 Elasticsearch 服务可用性。
 * <p>当 ES 连接正常时返回 UP 状态，异常时返回 DOWN 状态并记录详细原因。
 */
@Slf4j
@Component
public class CustomElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchClient elasticsearchClient;

    public CustomElasticsearchHealthIndicator(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * 执行 Elasticsearch 健康检查
     *
     * <p>检查流程：
     * <ol>
     *   <li>检查 ES 客户端是否初始化</li>
     *   <li>执行 ping 操作验证连接</li>
     *   <li>尝试获取 ES 版本信息（失败不影响健康状态）</li>
     * </ol>
     *
     * @return 健康状态对象
     */
    @Override
    public Health health() {
        if (elasticsearchClient == null) {
            log.warn("Elasticsearch 客户端未初始化，健康检查跳过");
            return Health.unknown()
                    .withDetail("reason", "ElasticsearchClient未初始化")
                    .build();
        }

        try {
            boolean isConnected = elasticsearchClient.ping().value();
            if (!isConnected) {
                log.warn("Elasticsearch ping 返回 false");
                return Health.down()
                        .withDetail("reason", "Ping返回false")
                        .build();
            }

            Health.Builder builder = Health.up()
                    .withDetail("status", "connected");

            // 获取版本信息（失败不影响整体健康状态）
            try {
                String version = elasticsearchClient.info().version().number();
                builder.withDetail("version", version);
            } catch (Exception e) {
                log.debug("获取 Elasticsearch 版本信息失败: {}", e.getMessage());
            }

            return builder.build();

        } catch (IOException e) {
            log.warn("Elasticsearch 健康检查失败: {}", e.getMessage());
            return Health.down()
                    .withDetail("reason", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.warn("Elasticsearch 健康检查异常: {}", e.getMessage());
            return Health.down()
                    .withDetail("reason", "Unexpected error: " + e.getMessage())
                    .build();
        }
    }
}