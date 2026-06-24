package com.tx.outsourcingmis.service.impl;

import com.tx.outsourcingmis.dto.PerformanceEvaluateRequest;
import com.tx.outsourcingmis.dto.PerformanceResponse;
import com.tx.outsourcingmis.entity.Performance;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.PerformanceMapper;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.PerformanceService;
import com.tx.outsourcingmis.utils.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 绩效管理服务实现类
 *
 * <p>核心功能：绩效评定（使用 Redis 分布式锁防止并发冲突）
 * <p>技术亮点：Redis 分布式锁保障同一用户同一月份不被并发评定
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceMapper performanceMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 分布式锁 Key 前缀 */
    private static final String LOCK_KEY_PREFIX = "lock:performance:";

    /** 锁过期时间（秒），防止死锁 */
    private static final long LOCK_EXPIRE_SECONDS = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PerformanceResponse evaluate(PerformanceEvaluateRequest request) {
        Long evaluatorId = UserContextHolder.getCurrentUserId();
        String evaluatorName = UserContextHolder.getCurrentUsername();

        Long userId = request.getUserId();
        String lockKey = LOCK_KEY_PREFIX + userId + ":" + request.getEvaluateYear() + ":" + request.getEvaluateMonth();

        // 尝试获取分布式锁（SET NX EX 原子操作）
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            throw new RuntimeException("该员工该月绩效正在被评定中，请稍后再试");
        }

        try {
            // 检查是否已存在该月绩效（防重复插入）
            Performance existing = performanceMapper.selectByUserAndPeriod(
                    userId, request.getEvaluateYear(), request.getEvaluateMonth());
            if (existing != null) {
                throw new RuntimeException("该月绩效已经评定过了");
            }

            User targetUser = userMapper.selectById(userId);
            if (targetUser == null) {
                throw new RuntimeException("被评定用户不存在");
            }

            validateGrade(request.getGrade());

            // 创建绩效记录
            Performance performance = new Performance();
            performance.setUserId(userId);
            performance.setEvaluatorId(evaluatorId);
            performance.setGrade(request.getGrade());
            performance.setComment(request.getComment());
            performance.setEvaluateYear(request.getEvaluateYear());
            performance.setEvaluateMonth(request.getEvaluateMonth());
            performance.setStatus(0);
            performance.setCreateTime(LocalDateTime.now());

            if (performanceMapper.insert(performance) <= 0) {
                throw new RuntimeException("绩效评定失败");
            }

            log.info("绩效评定成功 - userId: {}, grade: {}, period: {}-{}",
                    userId, request.getGrade(), request.getEvaluateYear(), request.getEvaluateMonth());

            return convertToResponse(performance, targetUser, evaluatorName);

        } finally {
            // 释放分布式锁
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPerformance(Long performanceId) {
        Long evaluatorId = UserContextHolder.getCurrentUserId();
        Performance performance = performanceMapper.selectById(performanceId);
        if (performance == null) {
            throw new RuntimeException("绩效记录不存在");
        }
        if (performance.getStatus() == 1) {
            throw new RuntimeException("绩效已确认");
        }
        // 只有评定人本人才能确认
        if (!performance.getEvaluatorId().equals(evaluatorId)) {
            throw new RuntimeException("只有评定人才能确认");
        }

        performance.setStatus(1);
        performance.setUpdateTime(LocalDateTime.now());
        performanceMapper.updateById(performance);
        log.info("绩效确认成功 - performanceId: {}", performanceId);
    }

    @Override
    public List<PerformanceResponse> getUserPerformances(Long userId) {
        User user = userMapper.selectById(userId);
        return performanceMapper.selectByUserId(userId).stream()
                .map(p -> {
                    User evaluator = userMapper.selectById(p.getEvaluatorId());
                    return convertToResponse(p, user, getUserName(evaluator));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceResponse> getPerformancesByPeriod(Integer year, Integer month) {
        return performanceMapper.selectByPeriod(year, month).stream()
                .map(p -> {
                    User user = userMapper.selectById(p.getUserId());
                    User evaluator = userMapper.selectById(p.getEvaluatorId());
                    return convertToResponse(p, user, getUserName(evaluator));
                })
                .collect(Collectors.toList());
    }

    /**
     * 校验等级有效性（A/B/C/D/E，不区分大小写）
     *
     * @param grade 等级
     */
    private void validateGrade(String grade) {
        if (grade == null || !grade.matches("(?i)[ABCDE]")) {
            throw new RuntimeException("等级必须是A、B、C、D、E中的一个");
        }
    }

    /**
     * 获取用户显示名称
     *
     * @param user 用户实体
     * @return 用户名或"未知"
     */
    private String getUserName(User user) {
        return user != null ? user.getUsername() : "未知";
    }

    /**
     * 获取等级中文描述
     *
     * @param grade 等级
     * @return 中文描述
     */
    private String getGradeDesc(String grade) {
        if (grade == null) {
            return "未知";
        }
        return switch (grade.toUpperCase()) {
            case "A" -> "优秀";
            case "B" -> "良好";
            case "C" -> "合格";
            case "D" -> "待提升";
            case "E" -> "不合格";
            default -> "未知";
        };
    }

    /**
     * 获取状态中文描述
     *
     * @param status 状态码
     * @return 中文描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "已确认";
            default -> "未知";
        };
    }

    /**
     * 转换为绩效响应对象
     *
     * @param performance 绩效实体
     * @param user 被评定用户
     * @param evaluatorName 评定人姓名
     * @return 绩效响应
     */
    private PerformanceResponse convertToResponse(Performance performance, User user, String evaluatorName) {
        return PerformanceResponse.builder()
                .id(performance.getId())
                .userId(performance.getUserId())
                .userName(getUserName(user))
                .evaluatorId(performance.getEvaluatorId())
                .evaluatorName(evaluatorName)
                .grade(performance.getGrade())
                .gradeDesc(getGradeDesc(performance.getGrade()))
                .comment(performance.getComment())
                .evaluateYear(performance.getEvaluateYear())
                .evaluateMonth(performance.getEvaluateMonth())
                .status(performance.getStatus())
                .statusDesc(getStatusDesc(performance.getStatus()))
                .createTime(performance.getCreateTime())
                .updateTime(performance.getUpdateTime())
                .build();
    }
}