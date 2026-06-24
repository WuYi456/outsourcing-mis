package com.tx.outsourcingmis.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批通知消息体
 *
 * <p>通过 RabbitMQ 发送审批状态变更通知，实现审批流程异步解耦。
 * <p>消息类型：
 * <ul>
 *   <li>PENDING —— 待审批通知（通知领导）</li>
 *   <li>APPROVED —— 审批通过通知（通知申请人）</li>
 *   <li>REJECTED —— 审批驳回通知（通知申请人）</li>
 * </ul>
 */
@Data
@Builder
public class ApprovalNotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请 ID */
    private Long applicationId;

    /** 审批人 ID */
    private Long approverId;

    /** 审批人姓名 */
    private String approverName;

    /** 申请人 ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 申请理由 */
    private String reason;

    /** 申请时间 */
    private LocalDateTime applyTime;

    /** 消息类型：PENDING-待审批，APPROVED-已通过，REJECTED-已拒绝 */
    private String messageType;

    /** 拒绝原因（仅 REJECTED 类型时有值） */
    private String rejectReason;
}