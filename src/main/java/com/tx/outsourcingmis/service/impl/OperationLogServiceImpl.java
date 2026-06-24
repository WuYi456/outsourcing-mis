package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.dto.OperationLogQueryRequest;
import com.tx.outsourcingmis.dto.OperationLogResponse;
import com.tx.outsourcingmis.es.document.OperationLogDocument;
import com.tx.outsourcingmis.es.repository.OperationLogElasticsearchRepository;
import com.tx.outsourcingmis.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 *
 * <p>使用 Elasticsearch 存储和查询操作日志，支持异步保存和组合搜索。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogElasticsearchRepository esRepository;

    @Override
    @Async("taskExecutor")
    public void saveLogAsync(OperationLogDocument logDoc) {
        saveLog(logDoc);
    }

    @Override
    public void saveLog(OperationLogDocument logDoc) {
        if (esRepository == null || logDoc == null) {
            return;
        }
        try {
            if (logDoc.getCreateTime() == null) {
                logDoc.setCreateTime(LocalDateTime.now());
            }
            esRepository.save(logDoc);
        } catch (Exception e) {
            log.error("操作日志存入 ES 失败: {}", e.getMessage());
        }
    }

    @Override
    public OperationLogDocument getById(String id) {
        if (esRepository == null) {
            return null;
        }
        try {
            return esRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("查询日志失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Page<OperationLogResponse> getByUsername(String username, Integer pageNum, Integer pageSize) {
        if (esRepository == null) {
            return Page.empty();
        }
        try {
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("createTime").descending());
            return esRepository.findByUsername(username, pageable).map(this::convertToResponse);
        } catch (Exception e) {
            log.error("按用户名查询失败: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Page<OperationLogResponse> getByOperation(String operation, Integer pageNum, Integer pageSize) {
        if (esRepository == null) {
            return Page.empty();
        }
        try {
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("createTime").descending());
            return esRepository.findByOperation(operation, pageable).map(this::convertToResponse);
        } catch (Exception e) {
            log.error("按操作类型查询失败: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public Page<OperationLogResponse> search(OperationLogQueryRequest request) {
        if (esRepository == null) {
            return Page.empty();
        }
        try {
            Pageable pageable = PageRequest.of(
                    request.getPageNum() - 1,
                    request.getPageSize(),
                    Sort.by("createTime").descending()
            );

            String startTime = toISO(request.getStartTime());
            String endTime = toISO(request.getEndTime());

            boolean hasUsername = hasText(request.getUsername());
            boolean hasOperation = hasText(request.getOperation());
            boolean hasTimeRange = startTime != null && endTime != null;

            Page<OperationLogDocument> page;
            if (hasUsername && hasOperation && hasTimeRange) {
                page = esRepository.searchLogs(request.getUsername(), request.getOperation(),
                        startTime, endTime, pageable);
            } else if (hasUsername && hasOperation) {
                page = esRepository.findByUsernameAndOperation(request.getUsername(),
                        request.getOperation(), pageable);
            } else if (hasUsername && hasTimeRange) {
                page = esRepository.searchLogs(request.getUsername(), null, startTime, endTime, pageable);
            } else if (hasOperation && hasTimeRange) {
                page = esRepository.searchLogs(null, request.getOperation(), startTime, endTime, pageable);
            } else if (hasUsername) {
                page = esRepository.findByUsername(request.getUsername(), pageable);
            } else if (hasOperation) {
                page = esRepository.findByOperation(request.getOperation(), pageable);
            } else if (hasTimeRange) {
                page = esRepository.findByCreateTimeBetween(
                        LocalDateTime.parse(startTime), LocalDateTime.parse(endTime), pageable);
            } else {
                page = esRepository.findAll(pageable);
            }
            return page.map(this::convertToResponse);
        } catch (Exception e) {
            log.error("搜索日志失败: {}", e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public void deleteById(String id) {
        if (esRepository == null) {
            return;
        }
        try {
            esRepository.deleteById(id);
        } catch (Exception e) {
            log.error("删除日志失败: {}", e.getMessage());
        }
    }

    /**
     * 判断字符串是否有内容
     *
     * @param str 字符串
     * @return true-有内容，false-为空
     */
    private boolean hasText(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 将日期字符串转换为 ISO 格式
     *
     * <p>输入：2026-06-08 00:00:00 → 输出：2026-06-08T00:00:00
     *
     * @param dateStr 日期字符串
     * @return ISO 格式字符串
     */
    private String toISO(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return dateStr.contains("T") ? dateStr : dateStr.replace(" ", "T");
    }

    /**
     * 将 ES 文档转换为响应对象
     *
     * @param doc ES 日志文档
     * @return 日志响应
     */
    private OperationLogResponse convertToResponse(OperationLogDocument doc) {
        if (doc == null) {
            return null;
        }
        return OperationLogResponse.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .username(doc.getUsername())
                .operation(doc.getOperation())
                .method(doc.getMethod())
                .params(doc.getParams())
                .result(doc.getResult())
                .ip(doc.getIp())
                .duration(doc.getDuration())
                .success(doc.getSuccess())
                .errorMsg(doc.getErrorMsg())
                .createTime(doc.getCreateTime())
                .build();
    }
}