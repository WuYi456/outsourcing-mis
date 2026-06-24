package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限实体
 *
 * <p>对应数据库表 permission，定义系统中所有权限标识。
 * <p>权限标识格式：模块:操作，如 user:view、user:edit、approval:approve
 */
@Data
@TableName("permission")
@Schema(description = "权限实体")
public class Permission {

    /** 权限 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "权限ID")
    private Long id;

    /** 权限名称（中文描述） */
    @Schema(description = "权限名称")
    private String permissionName;

    /** 权限标识，如 user:view */
    @Schema(description = "权限标识，如 user:view")
    private String permissionCode;
}