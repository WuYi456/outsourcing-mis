package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 绩效评定表实体
 *
 * <p>对应数据库表 performance，记录外包人员月度绩效评定结果。
 * <p>等级说明：A-优秀，B-良好，C-合格，D-待提升，E-不合格
 */
@Data
@TableName("performance")
@Schema(description = "绩效评定表")
public class Performance {

    /** 绩效记录 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "绩效ID")
    private Long id;

    /** 被评定用户 ID */
    @Schema(description = "被评定用户ID")
    private Long userId;

    /** 评定人 ID */
    @Schema(description = "评定人ID")
    private Long evaluatorId;

    /** 等级：A/B/C/D/E */
    @Schema(description = "等级: A/B/C/D/E")
    private String grade;

    /** 评定意见 */
    @Schema(description = "评定意见")
    private String comment;

    /** 评定年份 */
    @Schema(description = "评定年份")
    private Integer evaluateYear;

    /** 评定月份（1-12） */
    @Schema(description = "评定月份")
    private Integer evaluateMonth;

    /** 状态：0-待确认，1-已确认 */
    @Schema(description = "状态: 0待确认 1已确认")
    private Integer status;

    /** 创建时间（自动填充） */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}