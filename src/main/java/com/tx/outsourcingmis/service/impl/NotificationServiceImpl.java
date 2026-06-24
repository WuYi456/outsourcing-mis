package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.dto.ApprovalNotificationMessage;
import com.tx.outsourcingmis.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.tx.outsourcingmis.config.RabbitMQConfig.APPROVAL_EXCHANGE;
import static com.tx.outsourcingmis.config.RabbitMQConfig.APPROVAL_ROUTING_KEY;

/**
 * 消息通知服务实现类
 *
 * <p>通过 RabbitMQ 发送审批通知，实现异步解耦。
 * <p>发送失败不影响主流程，仅记录错误日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendApprovalNotification(ApprovalNotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(APPROVAL_EXCHANGE, APPROVAL_ROUTING_KEY, message);
            log.info("MQ 通知发送成功 - applicationId: {}, type: {}",
                    message.getApplicationId(), message.getMessageType());
        } catch (Exception e) {
            // 发送失败不影响主流程，仅记录日志
            log.error("MQ 通知发送失败 - applicationId: {}", message.getApplicationId(), e);
        }
    }
}