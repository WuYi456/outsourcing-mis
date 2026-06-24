package com.tx.outsourcingmis.es.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * 操作日志 ES 文档
 *
 * <p>对应 Elasticsearch 索引 operation_log，存储系统操作日志。
 * <p>字段类型说明：
 * <ul>
 *   <li>Keyword：精确匹配，用于过滤和聚合</li>
 *   <li>Text：全文搜索，index=false 表示不建索引（仅存储，不查询）</li>
 *   <li>Long：数值类型</li>
 *   <li>Boolean：布尔类型</li>
 *   <li>Date：日期类型，支持范围查询</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "operation_log", createIndex = true)
public class OperationLogDocument {

    /** 文档 ID（UUID） */
    @Id
    private String id;

    /** 操作用户 ID */
    @Field(type = FieldType.Long)
    private Long userId;

    /** 操作用户名 */
    @Field(type = FieldType.Keyword)
    private String username;

    /** 操作类型（类名.方法名） */
    @Field(type = FieldType.Keyword)
    private String operation;

    /** 请求方法（HTTP Method + URI） */
    @Field(type = FieldType.Keyword)
    private String method;

    /** 请求参数（JSON 格式，不建索引） */
    @Field(type = FieldType.Text, index = false)
    private String params;

    /** 执行结果（JSON 格式，不建索引） */
    @Field(type = FieldType.Text, index = false)
    private String result;

    /** 客户端 IP 地址 */
    @Field(type = FieldType.Keyword)
    private String ip;

    /** 执行耗时（毫秒） */
    @Field(type = FieldType.Long)
    private Long duration;

    /** 是否执行成功 */
    @Field(type = FieldType.Boolean)
    private Boolean success;

    /** 错误信息（不建索引） */
    @Field(type = FieldType.Text, index = false)
    private String errorMsg;

    /** 操作时间（支持毫秒精度） */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}