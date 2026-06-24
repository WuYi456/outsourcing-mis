package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户角色关联表实体
 *
 * <p>对应数据库表 user_role，建立用户与角色的多对多关联。
 * <p>一个用户可拥有多个角色，通过此表查询用户所属角色。
 */
@Data
@TableName("user_role")
@Schema(description = "用户角色关联表")
public class UserRole {

    /** 关联 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Long id;

    /** 用户 ID，关联 user 表 */
    @Schema(description = "用户ID")
    private Long userId;

    /** 角色 ID，关联 role 表 */
    @Schema(description = "角色ID")
    private Long roleId;
}