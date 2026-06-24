// src/test/java/com/tx/outsourcingmis/config/ElasticsearchConfigTest.java
package com.tx.outsourcingmis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Elasticsearch配置测试")
class ElasticsearchConfigTest {

    @Test
    @DisplayName("配置类可以创建")
    void testConfigCreation() {
        ElasticsearchConfig config = new ElasticsearchConfig();
        assertThat(config).isNotNull();
    }
}