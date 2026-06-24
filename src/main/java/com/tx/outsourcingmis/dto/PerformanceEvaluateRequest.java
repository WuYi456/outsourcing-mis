package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 绩效评定请求 DTO
 *
 * <p>用于外包人员绩效评定，包含被评定用户、等级、年月等信息。
 * <p>等级范围：A（优秀）、B（良好）、C（合格）、D（待提升）、E（不合格）
 */
@Data
@Schema(description = "绩效评定请求")
public class PerformanceEvaluateRequest {

    /** 被评定用户 ID（必填） */
    @NotNull(message = "被评定用户ID不能为空")
    @Schema(description = "被评定用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /** 绩效等级：A/B/C/D/E（必填） */
    @NotNull(message = "等级不能为空")
    @Schema(description = "等级: A/B/C/D/E", requiredMode = Schema.RequiredMode.REQUIRED)
    private String grade;

    /** 评定意见（可选） */
    @Schema(description = "评定意见")
    private String comment;

    /** 评定年份（必填，范围 2020-2030） */
    @NotNull(message = "评定年份不能为空")
    @Min(value = 2020, message = "年份不能小于2020")
    @Max(value = 2030, message = "年份不能大于2030")
    @Schema(description = "评定年份", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer evaluateYear;

    /** 评定月份（必填，范围 1-12） */
    @NotNull(message = "评定月份不能为空")
    @Min(value = 1, message = "月份必须在1-12之间")
    @Max(value = 12, message = "月份必须在1-12之间")
    @Schema(description = "评定月份", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer evaluateMonth;
}