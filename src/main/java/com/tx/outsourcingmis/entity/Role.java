package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色实体
 *
 * <p>对应数据库表 role，定义系统角色。
 * <p>系统预置角色：ADMIN（管理员）、MANAGER（领导）、EMPLOYEE（外包人员）
 */
@Data
@TableName("role")
@Schema(description = "角色实体")
public class Role {

    /** 角色 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "角色ID")
    private Long id;

    /** 角色名称（如 ADMIN、MANAGER、EMPLOYEE） */
    @Schema(description = "角色名称")
    private String roleName;

    /** 角色描述 */
    @Schema(description = "角色描述")
    private String description;
}