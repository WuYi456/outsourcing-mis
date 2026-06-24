// src/test/java/com/tx/outsourcingmis/config/RedisConfigTest.java
package com.tx.outsourcingmis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Redis配置测试")
class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    @DisplayName("RedisTemplate配置")
    void redisTemplate() {
        // 使用一个简单的连接工厂避免真实连接
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(6379);

        RedisTemplate<String, Object> template = redisConfig.redisTemplate(factory);
        assertThat(template).isNotNull();
    }

    @Test
    @DisplayName("CacheManager配置")
    void cacheManager() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        RedisCacheManager cacheManager = redisConfig.cacheManager(factory);
        assertThat(cacheManager).isNotNull();
    }
}