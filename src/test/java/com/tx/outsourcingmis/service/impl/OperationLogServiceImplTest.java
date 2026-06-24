// src/test/java/com/tx/outsourcingmis/service/impl/OperationLogServiceImplTest.java
package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.es.document.OperationLogDocument;
import com.tx.outsourcingmis.es.repository.OperationLogElasticsearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志服务测试")
class OperationLogServiceImplTest {

    @Mock
    private OperationLogElasticsearchRepository esRepository;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

    @Test
    @DisplayName("保存日志 - ES可用")
    void saveLog_Success() {
        OperationLogDocument log = OperationLogDocument.builder()
                .id("1")
                .username("testuser")
                .operation("login")
                .createTime(LocalDateTime.now())
                .build();

        when(esRepository.save(any())).thenReturn(log);

        operationLogService.saveLog(log);

        verify(esRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("保存日志 - log为null时跳过")
    void saveLog_NullLog() {
        operationLogService.saveLog(null);
        verify(esRepository, never()).save(any());
    }

    @Test
    @DisplayName("异步保存日志")
    void saveLogAsync() {
        OperationLogDocument log = OperationLogDocument.builder()
                .id("1")
                .username("testuser")
                .build();

        operationLogService.saveLogAsync(log);
        // 异步方法，等待一小段时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        verify(esRepository, times(1)).save(any());
    }
}