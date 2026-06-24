package com.tx.outsourcingmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tx.outsourcingmis.entity.Performance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 绩效评定表 Mapper
 *
 * <p>提供绩效评定表的数据库操作接口，继承 MyBatis-Plus BaseMapper 获得基础 CRUD 能力。
 */
@Mapper
public interface PerformanceMapper extends BaseMapper<Performance> {

    /**
     * 查询某用户某月的绩效记录
     *
     * @param userId 用户 ID
     * @param year 评定年份
     * @param month 评定月份
     * @return 绩效记录，不存在时返回 null
     */
    @Select("SELECT * FROM performance WHERE user_id = #{userId} AND evaluate_year = #{year} AND evaluate_month = #{month}")
    Performance selectByUserAndPeriod(@Param("userId") Long userId,
                                      @Param("year") Integer year,
                                      @Param("month") Integer month);

    /**
     * 查询某用户的所有绩效（按年月倒序）
     *
     * @param userId 用户 ID
     * @return 绩效列表
     */
    @Select("SELECT * FROM performance WHERE user_id = #{userId} ORDER BY evaluate_year DESC, evaluate_month DESC")
    List<Performance> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询某月所有人员的绩效（按创建时间倒序）
     *
     * @param year 评定年份
     * @param month 评定月份
     * @return 绩效列表
     */
    @Select("SELECT * FROM performance WHERE evaluate_year = #{year} AND evaluate_month = #{month} ORDER BY create_time DESC")
    List<Performance> selectByPeriod(@Param("year") Integer year, @Param("month") Integer month);
}