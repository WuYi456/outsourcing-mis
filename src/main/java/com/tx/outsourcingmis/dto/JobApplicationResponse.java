package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上岗申请响应 DTO
 *
 * <p>包含申请完整信息，用于申请列表和详情查询响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "上岗申请响应")
public class JobApplicationResponse {

    /** 申请 ID */
    @Schema(description = "申请ID")
    private Long id;

    /** 申请人 ID */
    @Schema(description = "申请人ID")
    private Long applicantId;

    /** 申请人姓名 */
    @Schema(description = "申请人姓名")
    private String applicantName;

    /** 审批人 ID */
    @Schema(description = "审批人ID")
    private Long approverId;

    /** 审批人姓名 */
    @Schema(description = "审批人姓名")
    private String approverName;

    /** 申请理由 */
    @Schema(description = "申请理由")
    private String reason;

    /** 状态：0-待审批，1-已通过，2-已拒绝 */
    @Schema(description = "状态: 0待审批 1已通过 2已拒绝")
    private Integer status;

    /** 状态描述（中文） */
    @Schema(description = "状态描述")
    private String statusDesc;

    /** 拒绝原因（仅已拒绝状态时有值） */
    @Schema(description = "拒绝原因")
    private String rejectReason;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 申请时间 */
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /** 审批时间 */
    @Schema(description = "审批时间")
    private LocalDateTime approveTime;
}