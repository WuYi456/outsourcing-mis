package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批响应 DTO
 *
 * <p>用于返回审批列表数据，包含申请基本信息和审批状态。
 */
@Data
@Builder
@Schema(description = "审批响应")
public class ApprovalResponse {

    /** 申请 ID */
    @Schema(description = "申请ID")
    private Long id;

    /** 申请人姓名 */
    @Schema(description = "申请人姓名")
    private String applicantName;

    /** 申请理由 */
    @Schema(description = "申请理由")
    private String reason;

    /** 申请时间 */
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /** 状态：0-待审批，1-已通过，2-已拒绝 */
    @Schema(description = "状态: 0待审批 1已通过 2已拒绝")
    private Integer status;
}