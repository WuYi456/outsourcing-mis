package com.tx.outsourcingmis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密配置
 *
 * <p>使用 BCrypt 强哈希算法加密用户密码，保障密码存储安全。
 */
@Configuration
public class PasswordConfig {

    /**
     * 创建 BCrypt 密码编码器
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}