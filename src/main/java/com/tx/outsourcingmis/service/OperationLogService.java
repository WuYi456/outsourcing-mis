package com.tx.outsourcingmis.service;

import com.tx.outsourcingmis.dto.OperationLogQueryRequest;
import com.tx.outsourcingmis.dto.OperationLogResponse;
import com.tx.outsourcingmis.es.document.OperationLogDocument;
import org.springframework.data.domain.Page;

/**
 * 操作日志服务接口
 *
 * <p>提供操作日志的保存、查询和删除功能，数据存储在 Elasticsearch 中。
 */
public interface OperationLogService {

    /**
     * 异步保存操作日志到 ES
     *
     * @param logDoc 日志文档
     */
    void saveLogAsync(OperationLogDocument logDoc);

    /**
     * 同步保存操作日志到 ES
     *
     * @param logDoc 日志文档
     */
    void saveLog(OperationLogDocument logDoc);

    /**
     * 根据 ID 查询日志
     *
     * @param id 日志 ID
     * @return 日志文档，不存在时返回 null
     */
    OperationLogDocument getById(String id);

    /**
     * 根据用户名查询日志（分页）
     *
     * @param username 用户名
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页日志列表
     */
    Page<OperationLogResponse> getByUsername(String username, Integer pageNum, Integer pageSize);

    /**
     * 根据操作类型查询日志（分页）
     *
     * @param operation 操作类型
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页日志列表
     */
    Page<OperationLogResponse> getByOperation(String operation, Integer pageNum, Integer pageSize);

    /**
     * 高级搜索操作日志
     *
     * @param request 查询请求（支持用户名、操作类型、时间范围组合）
     * @return 分页日志列表
     */
    Page<OperationLogResponse> search(OperationLogQueryRequest request);

    /**
     * 删除操作日志
     *
     * @param id 日志 ID
     */
    void deleteById(String id);
}