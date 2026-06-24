package com.tx.outsourcingmis.mq;

import com.tx.outsourcingmis.dto.ApprovalNotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.tx.outsourcingmis.config.RabbitMQConfig.APPROVAL_QUEUE;

/**
 * 审批通知消费者
 *
 * <p>监听 RabbitMQ 审批通知队列，处理审批状态变更通知。
 * <p>支持三种消息类型：
 * <ul>
 *   <li>PENDING —— 待审批通知（通知领导）</li>
 *   <li>APPROVED —— 审批通过通知（通知申请人）</li>
 *   <li>REJECTED —— 审批驳回通知（通知申请人）</li>
 * </ul>
 */
@Slf4j
@Component
public class NotificationConsumer {

    /**
     * 处理审批通知消息
     *
     * @param message 审批通知消息体
     */
    @RabbitListener(queues = APPROVAL_QUEUE)
    public void handleApprovalNotification(ApprovalNotificationMessage message) {
        switch (message.getMessageType()) {
            case "PENDING" -> log.info("待审批通知 - 领导: {}, 申请人: {}, 理由: {}",
                    message.getApproverName(), message.getApplicantName(), message.getReason());
            case "APPROVED" -> log.info("审批通过通知 - 申请人: {}", message.getApplicantName());
            case "REJECTED" -> log.info("审批驳回通知 - 申请人: {}, 原因: {}",
                    message.getApplicantName(), message.getRejectReason());
            default -> log.warn("未知消息类型: {}", message.getMessageType());
        }
    }
}