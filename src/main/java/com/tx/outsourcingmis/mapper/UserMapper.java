package com.tx.outsourcingmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tx.outsourcingmis.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户表 Mapper
 *
 * <p>提供用户表的数据库操作接口，继承 MyBatis-Plus BaseMapper 获得基础 CRUD 能力。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息，不存在时返回 null
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据用户名查询启用状态的用户
     *
     * @param username 用户名
     * @return 用户信息，不存在或已禁用时返回 null
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND status = 1")
    User selectActiveByUsername(@Param("username") String username);
}