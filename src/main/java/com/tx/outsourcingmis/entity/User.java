package com.tx.outsourcingmis.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * <p>对应数据库表 user，存储系统用户信息。
 * <p>角色说明：ADMIN（管理员）、MANAGER（领导）、EMPLOYEE（外包人员）
 * <p>状态说明：1-启用，0-禁用
 */
@Data
@TableName("user")
@Schema(description = "用户实体")
public class User {

    /** 用户 ID（自增主键） */
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    /** 用户名（必填，唯一） */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** 密码（必填，写入时忽略返回） */
    @NotBlank(message = "密码不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /** 真实姓名 */
    @Schema(description = "真实姓名")
    private String realName;

    /** 邮箱（格式校验） */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    /** 手机号（格式校验） */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    /** 角色：ADMIN/MANAGER/EMPLOYEE */
    @Schema(description = "角色: ADMIN/MANAGER/EMPLOYEE")
    private String role;

    /** 状态：1-启用，0-禁用 */
    @Schema(description = "状态: 1启用 0禁用")
    private Integer status;

    /** 上级领导 ID（外包人员必填，用于审批流程） */
    @Schema(description = "上级领导ID")
    private Long leaderId;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}