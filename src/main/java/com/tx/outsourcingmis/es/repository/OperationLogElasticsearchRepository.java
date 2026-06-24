package com.tx.outsourcingmis.es.repository;

import com.tx.outsourcingmis.es.document.OperationLogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 操作日志 ES 数据访问层
 *
 * <p>提供操作日志在 Elasticsearch 中的 CRUD 和查询方法。
 * <p>继承 ElasticsearchRepository 自动获得基础 CRUD 能力。
 */
@Repository
public interface OperationLogElasticsearchRepository
        extends ElasticsearchRepository<OperationLogDocument, String> {

    /**
     * 根据用户名查询日志（分页）
     *
     * @param username 用户名
     * @param pageable 分页参数
     * @return 分页日志列表
     */
    Page<OperationLogDocument> findByUsername(String username, Pageable pageable);

    /**
     * 根据操作类型查询日志（分页）
     *
     * @param operation 操作类型
     * @param pageable 分页参数
     * @return 分页日志列表
     */
    Page<OperationLogDocument> findByOperation(String operation, Pageable pageable);

    /**
     * 根据用户名和操作类型组合查询（分页）
     *
     * @param username 用户名
     * @param operation 操作类型
     * @param pageable 分页参数
     * @return 分页日志列表
     */
    Page<OperationLogDocument> findByUsernameAndOperation(String username, String operation, Pageable pageable);

    /**
     * 根据时间范围查询日志（分页）
     *
     * @param start 开始时间
     * @param end 结束时间
     * @param pageable 分页参数
     * @return 分页日志列表
     */
    Page<OperationLogDocument> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 高级组合搜索
     *
     * <p>支持用户名、操作类型、时间范围组合查询。
     * <p>参数传 null 或空字符串表示忽略该条件。
     *
     * @param username 用户名（可选）
     * @param operation 操作类型（可选）
     * @param startTime 开始时间（ISO 格式，可选）
     * @param endTime 结束时间（ISO 格式，可选）
     * @param pageable 分页参数
     * @return 分页日志列表
     */
    @Query("{\"bool\": {\"must\": [" +
            "{\"match\": {\"username\": \"?0\"}}," +
            "{\"match\": {\"operation\": \"?1\"}}," +
            "{\"range\": {\"create_time\": {\"gte\": \"?2\", \"lte\": \"?3\"}}}" +
            "]}}")
    Page<OperationLogDocument> searchLogs(String username, String operation,
                                          String startTime, String endTime,
                                          Pageable pageable);
}