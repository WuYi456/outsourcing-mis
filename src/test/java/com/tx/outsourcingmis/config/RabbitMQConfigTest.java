// src/test/java/com/tx/outsourcingmis/config/RabbitMQConfigTest.java
package com.tx.outsourcingmis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RabbitMQ配置测试")
class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    @DisplayName("审批队列创建")
    void approvalQueue() {
        Queue queue = config.approvalQueue();
        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.APPROVAL_QUEUE);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("审批交换机创建")
    void approvalExchange() {
        DirectExchange exchange = config.approvalExchange();
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.APPROVAL_EXCHANGE);
    }

    @Test
    @DisplayName("审批绑定创建")
    void approvalBinding() {
        Binding binding = config.approvalBinding();
        assertThat(binding).isNotNull();
        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.APPROVAL_QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.APPROVAL_ROUTING_KEY);
    }

    @Test
    @DisplayName("消息转换器创建")
    void messageConverter() {
        MessageConverter converter = config.messageConverter();
        assertThat(converter).isNotNull();
        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}