package com.tx.outsourcingmis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应 DTO
 *
 * <p>登录成功后返回 JWT Token 及用户基本信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /** JWT 认证令牌 */
    @Schema(description = "JWT Token")
    private String token;

    /** 用户 ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 用户名 */
    @Schema(description = "用户名")
    private String username;

    /** 角色名称 */
    @Schema(description = "角色")
    private String role;

    /** 权限标识列表 */
    @Schema(description = "权限列表")
    private List<String> permissions;
}