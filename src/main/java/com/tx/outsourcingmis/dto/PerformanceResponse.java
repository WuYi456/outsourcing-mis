package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 绩效评定响应 DTO
 *
 * <p>包含绩效评定的完整信息，用于查询和展示。
 */
@Data
@Builder
@Schema(description = "绩效评定响应")
public class PerformanceResponse {

    /** 绩效记录 ID */
    @Schema(description = "绩效ID")
    private Long id;

    /** 被评定用户 ID */
    @Schema(description = "被评定用户ID")
    private Long userId;

    /** 被评定用户姓名 */
    @Schema(description = "被评定用户姓名")
    private String userName;

    /** 评定人 ID */
    @Schema(description = "评定人ID")
    private Long evaluatorId;

    /** 评定人姓名 */
    @Schema(description = "评定人姓名")
    private String evaluatorName;

    /** 等级：A/B/C/D/E */
    @Schema(description = "等级")
    private String grade;

    /** 等级中文描述 */
    @Schema(description = "等级描述")
    private String gradeDesc;

    /** 评定意见 */
    @Schema(description = "评定意见")
    private String comment;

    /** 评定年份 */
    @Schema(description = "评定年份")
    private Integer evaluateYear;

    /** 评定月份 */
    @Schema(description = "评定月份")
    private Integer evaluateMonth;

    /** 状态：0-待确认，1-已确认 */
    @Schema(description = "状态: 0待确认 1已确认")
    private Integer status;

    /** 状态中文描述 */
    @Schema(description = "状态描述")
    private String statusDesc;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}