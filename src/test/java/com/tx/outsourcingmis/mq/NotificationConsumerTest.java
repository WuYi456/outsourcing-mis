// src/test/java/com/tx/outsourcingmis/mq/NotificationConsumerTest.java
package com.tx.outsourcingmis.mq;

import com.tx.outsourcingmis.dto.ApprovalNotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
@DisplayName("MQ消费者测试")
class NotificationConsumerTest {

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    @DisplayName("处理待审批通知")
    void handlePendingNotification() {
        ApprovalNotificationMessage message = ApprovalNotificationMessage.builder()
                .applicationId(1L)
                .approverId(200L)
                .approverName("领导")
                .applicantId(100L)
                .applicantName("员工")
                .reason("申请理由")
                .applyTime(LocalDateTime.now())
                .messageType("PENDING")
                .build();

        // 只是调用，验证不抛异常
        notificationConsumer.handleApprovalNotification(message);
    }

    @Test
    @DisplayName("处理审批通过通知")
    void handleApprovedNotification() {
        ApprovalNotificationMessage message = ApprovalNotificationMessage.builder()
                .applicationId(1L)
                .applicantId(100L)
                .applicantName("员工")
                .messageType("APPROVED")
                .build();

        notificationConsumer.handleApprovalNotification(message);
    }

    @Test
    @DisplayName("处理审批拒绝通知")
    void handleRejectedNotification() {
        ApprovalNotificationMessage message = ApprovalNotificationMessage.builder()
                .applicationId(1L)
                .applicantId(100L)
                .applicantName("员工")
                .messageType("REJECTED")
                .rejectReason("能力不匹配")
                .build();

        notificationConsumer.handleApprovalNotification(message);
    }

    @Test
    @DisplayName("处理未知类型通知")
    void handleUnknownTypeNotification() {
        ApprovalNotificationMessage message = ApprovalNotificationMessage.builder()
                .applicationId(1L)
                .messageType("UNKNOWN")
                .build();

        notificationConsumer.handleApprovalNotification(message);
    }
}