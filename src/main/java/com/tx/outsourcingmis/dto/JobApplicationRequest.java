package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 上岗申请请求 DTO
 *
 * <p>外包人员提交上岗申请时使用，包含申请理由和备注。
 */
@Data
@Schema(description = "上岗申请请求")
public class JobApplicationRequest {

    /** 申请理由（必填） */
    @NotBlank(message = "申请理由不能为空")
    @Schema(description = "申请理由", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    /** 备注（可选） */
    @Schema(description = "备注")
    private String remark;
}