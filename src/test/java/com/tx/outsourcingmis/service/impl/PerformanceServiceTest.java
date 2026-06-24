// src/test/java/com/tx/outsourcingmis/service/impl/PerformanceServiceTest.java
package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.dto.PerformanceEvaluateRequest;
import com.tx.outsourcingmis.dto.PerformanceResponse;
import com.tx.outsourcingmis.entity.Performance;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.PerformanceMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.PerformanceService;
import com.tx.outsourcingmis.utils.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("绩效服务单元测试")
class PerformanceServiceTest {

    @Mock
    private PerformanceMapper performanceMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PerformanceService performanceService;

    private User targetUser;
    private Performance testPerformance;
    private PerformanceEvaluateRequest request;

    @BeforeEach
    void setUp() {
        targetUser = new User();
        targetUser.setId(100L);
        targetUser.setUsername("employee1");
        targetUser.setRealName("测试员工");

        testPerformance = new Performance();
        testPerformance.setId(1L);
        testPerformance.setUserId(100L);
        testPerformance.setEvaluatorId(200L);
        testPerformance.setGrade("B");
        testPerformance.setEvaluateYear(2026);
        testPerformance.setEvaluateMonth(6);
        testPerformance.setStatus(0);

        request = new PerformanceEvaluateRequest();
        request.setUserId(100L);
        request.setGrade("B");
        request.setComment("工作表现良好");
        request.setEvaluateYear(2026);
        request.setEvaluateMonth(6);

        UserContextHolder.setCurrentUserId(200L);
        UserContextHolder.setCurrentUsername("leader1");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("评定绩效 - 成功获取分布式锁并评定")
    void evaluate_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("locked"), eq(5L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(performanceMapper.selectByUserAndPeriod(100L, 2026, 6)).thenReturn(null);
        when(userMapper.selectById(100L)).thenReturn(targetUser);
        when(performanceMapper.insert(any(Performance.class))).thenReturn(1);

        PerformanceResponse response = performanceService.evaluate(request);

        assertThat(response).isNotNull();
        assertThat(response.getGrade()).isEqualTo("B");
        assertThat(response.getGradeDesc()).contains("超出预期");

        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("评定绩效 - 分布式锁获取失败，应抛出异常")
    void evaluate_LockFailed_ThrowsException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("locked"), eq(5L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        assertThatThrownBy(() -> performanceService.evaluate(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("正在被评定中");

        verify(performanceMapper, never()).insert(any());
    }

    @Test
    @DisplayName("评定绩效 - 已存在该月绩效，应抛出异常")
    void evaluate_DuplicatePeriod_ThrowsException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("locked"), eq(5L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(performanceMapper.selectByUserAndPeriod(100L, 2026, 6)).thenReturn(testPerformance);

        assertThatThrownBy(() -> performanceService.evaluate(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已经评定过了");

        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("评定绩效 - 等级无效，应抛出异常")
    void evaluate_InvalidGrade_ThrowsException() {
        request.setGrade("F");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("locked"), eq(5L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(performanceMapper.selectByUserAndPeriod(100L, 2026, 6)).thenReturn(null);
        when(userMapper.selectById(100L)).thenReturn(targetUser);

        assertThatThrownBy(() -> performanceService.evaluate(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("等级必须是A、B、C、D、E中的一个");
    }

    @Test
    @DisplayName("确认绩效 - 成功")
    void confirmPerformance_Success() {
        when(performanceMapper.selectById(1L)).thenReturn(testPerformance);
        when(performanceMapper.updateById(any(Performance.class))).thenReturn(1);

        performanceService.confirmPerformance(1L);

        verify(performanceMapper, times(1)).updateById(argThat(p -> p.getStatus() == 1));
    }

    @Test
    @DisplayName("确认绩效 - 绩效已确认，应抛出异常")
    void confirmPerformance_AlreadyConfirmed_ThrowsException() {
        testPerformance.setStatus(1);
        when(performanceMapper.selectById(1L)).thenReturn(testPerformance);

        assertThatThrownBy(() -> performanceService.confirmPerformance(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("绩效已经确认过了");
    }

    @Test
    @DisplayName("确认绩效 - 非评定人确认，应抛出异常")
    void confirmPerformance_NotEvaluator_ThrowsException() {
        UserContextHolder.setCurrentUserId(999L);
        when(performanceMapper.selectById(1L)).thenReturn(testPerformance);

        assertThatThrownBy(() -> performanceService.confirmPerformance(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("只有评定人本人才能确认绩效");
    }

    @Test
    @DisplayName("获取用户绩效 - 成功")
    void getUserPerformances_Success() {
        List<Performance> performances = List.of(testPerformance);
        when(performanceMapper.selectByUserId(100L)).thenReturn(performances);
        when(userMapper.selectById(100L)).thenReturn(targetUser);
        when(userMapper.selectById(200L)).thenReturn(null);

        List<PerformanceResponse> responses = performanceService.getUserPerformances(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserName()).isEqualTo("employee1");
    }

    @Test
    @DisplayName("获取某月所有人员绩效 - 成功")
    void getPerformancesByPeriod_Success() {
        List<Performance> performances = List.of(testPerformance);
        when(performanceMapper.selectByPeriod(2026, 6)).thenReturn(performances);
        when(userMapper.selectById(100L)).thenReturn(targetUser);
        when(userMapper.selectById(200L)).thenReturn(null);

        List<PerformanceResponse> responses = performanceService.getPerformancesByPeriod(2026, 6);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEvaluateYear()).isEqualTo(2026);
        assertThat(responses.get(0).getEvaluateMonth()).isEqualTo(6);
    }
}