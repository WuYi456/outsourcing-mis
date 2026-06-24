package com.tx.outsourcingmis.service;

import com.tx.outsourcingmis.dto.ApprovalNotificationMessage;

/**
 * 消息通知服务接口
 *
 * <p>通过 RabbitMQ 发送审批相关的异步通知。
 */
public interface NotificationService {

    /**
     * 发送审批通知
     *
     * @param message 通知消息体
     */
    void sendApprovalNotification(ApprovalNotificationMessage message);
}