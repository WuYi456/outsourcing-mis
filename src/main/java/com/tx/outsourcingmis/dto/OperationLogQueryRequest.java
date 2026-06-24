package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志查询请求 DTO
 *
 * <p>支持按用户名、操作类型、时间范围组合查询，所有条件均为可选。
 */
@Data
@Schema(description = "操作日志查询请求")
public class OperationLogQueryRequest {

    /** 用户名（可选） */
    @Schema(description = "用户名")
    private String username;

    /** 操作类型（可选，如 UserController.login） */
    @Schema(description = "操作类型")
    private String operation;

    /** 开始时间（可选，格式：yyyy-MM-dd HH:mm:ss） */
    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    /** 结束时间（可选，格式：yyyy-MM-dd HH:mm:ss） */
    @Schema(description = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    /** 页码，默认 1 */
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    /** 每页大小，默认 20 */
    @Schema(description = "每页大小", defaultValue = "20")
    private Integer pageSize = 20;
}