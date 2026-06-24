// src/test/java/com/tx/outsourcingmis/config/PasswordConfigTest.java
package com.tx.outsourcingmis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("密码配置测试")
class PasswordConfigTest {

    private final PasswordConfig passwordConfig = new PasswordConfig();

    @Test
    @DisplayName("BCryptPasswordEncoder创建成功")
    void passwordEncoder() {
        BCryptPasswordEncoder encoder = passwordConfig.passwordEncoder();
        assertThat(encoder).isNotNull();

        String raw = "123456";
        String encoded = encoder.encode(raw);
        assertThat(encoded).isNotEqualTo(raw);
        assertThat(encoder.matches(raw, encoded)).isTrue();
    }
}