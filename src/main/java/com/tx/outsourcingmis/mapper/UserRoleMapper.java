package com.tx.outsourcingmis.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户角色关联 Mapper
 *
 * <p>提供用户与角色关联关系的查询和操作接口。
 */
@Mapper
public interface UserRoleMapper {

    /**
     * 根据用户 ID 查询角色名称
     *
     * @param userId 用户 ID
     * @return 角色名称，不存在时返回 null
     */
    @Select("SELECT r.role_name FROM role r JOIN user_role ur ON ur.role_id = r.id WHERE ur.user_id = #{userId}")
    String getRoleByUserId(@Param("userId") Long userId);

    /**
     * 根据角色名称查询角色 ID
     *
     * @param roleName 角色名称（如 ADMIN、MANAGER、EMPLOYEE）
     * @return 角色 ID，不存在时返回 null
     */
    @Select("SELECT id FROM role WHERE role_name = #{roleName}")
    Long getRoleIdByRoleName(@Param("roleName") String roleName);

    /**
     * 为用户分配角色
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     */
    @Insert("INSERT INTO user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void assignRoleToUser(@Param("userId") Long userId, @Param("roleId") Long roleId);
}