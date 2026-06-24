package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 审批请求 DTO
 *
 * <p>支持三种审批方式：
 * <ul>
 *   <li>单个审批：通过 {@link #applicationId} 指定单个申请 ID</li>
 *   <li>批量审批：通过 {@link #applicationIds} 指定多个申请 ID</li>
 *   <li>全选审批：通过 {@link #selectAll} = true 审批所有待审批申请</li>
 * </ul>
 * <p>拒绝时需填写 {@link #rejectReason}。
 */
@Data
@Schema(description = "审批请求")
public class ApprovalRequest {

    /** 申请 ID（单个审批时使用） */
    @Schema(description = "申请ID（单个审批时使用）")
    private Long applicationId;

    /** 申请 ID 列表（批量审批时使用） */
    @Schema(description = "申请ID列表（批量审批时使用）")
    private List<Long> applicationIds;

    /** 是否全选（true 表示审批所有待审批申请） */
    @Schema(description = "是否全选（true表示审批所有待审批申请）")
    private Boolean selectAll;

    /** 拒绝原因（仅拒绝时需要） */
    @Schema(description = "拒绝原因（仅拒绝时需要）")
    private String rejectReason;
}