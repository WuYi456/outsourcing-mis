package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志查询响应 DTO
 *
 * <p>从 Elasticsearch 查询返回的操作日志数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "操作日志查询响应")
public class OperationLogResponse {

    /** 日志 ID（ES 文档 ID） */
    @Schema(description = "日志ID")
    private String id;

    /** 操作用户 ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 操作用户名 */
    @Schema(description = "用户名")
    private String username;

    /** 操作类型（类名.方法名） */
    @Schema(description = "操作类型")
    private String operation;

    /** 请求方法（HTTP Method + URI） */
    @Schema(description = "请求方法")
    private String method;

    /** 请求参数（JSON 格式，超长截断） */
    @Schema(description = "请求参数")
    private String params;

    /** 执行结果（JSON 格式，超长截断） */
    @Schema(description = "执行结果")
    private String result;

    /** 客户端 IP 地址 */
    @Schema(description = "客户端IP")
    private String ip;

    /** 执行耗时（毫秒） */
    @Schema(description = "执行耗时(毫秒)")
    private Long duration;

    /** 是否执行成功 */
    @Schema(description = "是否成功")
    private Boolean success;

    /** 错误信息（执行失败时） */
    @Schema(description = "错误信息")
    private String errorMsg;

    /** 操作时间 */
    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}