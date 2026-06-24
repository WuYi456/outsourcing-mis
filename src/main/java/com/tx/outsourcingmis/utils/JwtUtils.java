package com.tx.outsourcingmis.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类
 *
 * <p>提供 JWT Token 的生成、解析和验证功能。
 * <p>使用 HS512 签名算法，Token 中包含用户 ID、用户名、角色和权限列表。
 */
@Slf4j
@Component
public class JwtUtils {

    /** JWT 签名密钥（Base64 编码） */
    @Value("${jwt.secret}")
    private String secret;

    /** Token 过期时间（毫秒），默认 2 小时 */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @param role 角色
     * @param permissions 权限列表
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String role, List<String> permissions) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .claim("permissions", permissions)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token JWT Token
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return (String) parseToken(token).get("username");
    }

    /**
     * 从 Token 中获取角色
     *
     * @param token JWT Token
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        return (String) parseToken(token).get("role");
    }

    /**
     * 从 Token 中获取权限列表
     *
     * @param token JWT Token
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        return (List<String>) parseToken(token).get("permissions");
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期");
        } catch (JwtException e) {
            log.warn("Token 验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析 JWT Token
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}