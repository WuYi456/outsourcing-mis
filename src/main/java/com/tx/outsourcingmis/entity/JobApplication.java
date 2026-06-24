package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上岗申请表实体
 *
 * <p>对应数据库表 job_application，记录外包人员上岗申请信息。
 */
@Data
@TableName("job_application")
@Schema(description = "上岗申请表")
public class JobApplication {

    /** 申请 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "申请ID")
    private Long id;

    /** 申请人 ID */
    @Schema(description = "申请人ID")
    private Long applicantId;

    /** 审批人 ID（申请人的上级领导） */
    @Schema(description = "审批人ID")
    private Long approverId;

    /** 申请理由 */
    @Schema(description = "申请理由")
    private String reason;

    /** 状态：0-待审批，1-已通过，2-已拒绝 */
    @Schema(description = "状态: 0待审批 1已通过 2已拒绝")
    private Integer status;

    /** 拒绝原因（仅状态为已拒绝时有值） */
    @Schema(description = "拒绝原因")
    private String rejectReason;

    /** 申请时间（默认当前时间） */
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /** 审批时间（审批通过或拒绝时记录） */
    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}