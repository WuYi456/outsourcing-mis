package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色权限关联表实体
 *
 * <p>对应数据库表 role_permission，建立角色与权限的多对多关联。
 * <p>通过此表查询角色拥有的权限列表，实现 RBAC 权限控制。
 */
@Data
@TableName("role_permission")
@Schema(description = "角色权限关联表")
public class RolePermission {

    /** 关联 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Long id;

    /** 角色 ID，关联 role 表 */
    @Schema(description = "角色ID")
    private Long roleId;

    /** 权限 ID，关联 permission 表 */
    @Schema(description = "权限ID")
    private Long permissionId;
}