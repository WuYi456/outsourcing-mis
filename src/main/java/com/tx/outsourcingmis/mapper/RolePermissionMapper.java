package com.tx.outsourcingmis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联 Mapper
 *
 * <p>提供角色和权限的查询接口，用于 RBAC 权限控制。
 */
@Mapper
public interface RolePermissionMapper {

    /**
     * 根据角色名查询权限码列表
     *
     * @param role 角色名称（如 ADMIN、MANAGER、EMPLOYEE）
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT p.permission_code " +
            "FROM role r " +
            "LEFT JOIN role_permission rp ON rp.role_id = r.id " +
            "LEFT JOIN permission p ON rp.permission_id = p.id " +
            "WHERE r.role_name = #{role} AND p.permission_code IS NOT NULL")
    List<String> getPermissionsByRole(@Param("role") String role);

    /**
     * 根据用户 ID 查询权限码列表
     *
     * <p>通过用户 → 用户角色 → 角色 → 角色权限 → 权限 多表关联查询。
     *
     * @param userId 用户 ID
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT p.permission_code " +
            "FROM user u " +
            "LEFT JOIN user_role ur ON ur.user_id = u.id " +
            "LEFT JOIN role r ON ur.role_id = r.id " +
            "LEFT JOIN role_permission rp ON rp.role_id = r.id " +
            "LEFT JOIN permission p ON rp.permission_id = p.id " +
            "WHERE u.id = #{userId} AND p.permission_code IS NOT NULL")
    List<String> getPermissionsByUserId(@Param("userId") Long userId);
}