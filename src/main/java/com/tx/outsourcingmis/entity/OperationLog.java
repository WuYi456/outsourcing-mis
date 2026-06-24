package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表实体（MySQL 备份表）
 *
 * <p>注意：操作日志实际存储已迁移至 Elasticsearch（OperationLogDocument）。
 * <p>此表仅作为历史数据备份或降级方案保留，新日志不再写入此表。
 */
@Data
@TableName("operation_log")
@Schema(description = "操作日志表")
public class OperationLog {

    /** 日志 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    /** 操作用户 ID */
    @Schema(description = "操作用户ID")
    private Long userId;

    /** 操作用户名 */
    @Schema(description = "操作用户名")
    private String username;

    /** 操作类型（类名.方法名） */
    @Schema(description = "操作类型")
    private String operation;

    /** 请求方法（HTTP Method + URI） */
    @Schema(description = "请求方法")
    private String method;

    /** 请求参数（JSON 格式） */
    @Schema(description = "请求参数")
    private String params;

    /** 客户端 IP 地址 */
    @Schema(description = "客户端IP")
    private String ip;

    /** 执行耗时（毫秒） */
    @Schema(description = "执行耗时(毫秒)")
    private Long duration;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}