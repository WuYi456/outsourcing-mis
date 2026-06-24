package com.tx.outsourcingmis.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 *
 * <p>用于审批通知的异步消息分发，解耦审批主流程。
 * <p>使用 Direct Exchange + 持久化队列，确保消息不丢失。
 */
@Configuration
public class RabbitMQConfig {

    /** 审批通知队列名称 */
    public static final String APPROVAL_QUEUE = "approval.notification.queue";

    /** 审批通知交换机名称 */
    public static final String APPROVAL_EXCHANGE = "approval.notification.exchange";

    /** 审批通知路由键 */
    public static final String APPROVAL_ROUTING_KEY = "approval.notification";

    /**
     * 创建审批通知队列（持久化）
     *
     * @return Queue
     */
    @Bean
    public Queue approvalQueue() {
        return QueueBuilder.durable(APPROVAL_QUEUE).build();
    }

    /**
     * 创建直连交换机
     *
     * @return DirectExchange
     */
    @Bean
    public DirectExchange approvalExchange() {
        return new DirectExchange(APPROVAL_EXCHANGE);
    }

    /**
     * 绑定队列到交换机
     *
     * @return Binding
     */
    @Bean
    public Binding approvalBinding() {
        return BindingBuilder.bind(approvalQueue())
                .to(approvalExchange())
                .with(APPROVAL_ROUTING_KEY);
    }

    /**
     * 消息转换器（JSON 格式）
     *
     * @return Jackson2JsonMessageConverter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 模板（统一消息转换）
     *
     * @param connectionFactory 连接工厂
     * @return RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    /**
     * 监听器容器工厂（统一消息转换）
     *
     * @param connectionFactory 连接工厂
     * @return SimpleRabbitListenerContainerFactory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}