// src/test/java/com/tx/outsourcingmis/config/MyMetaObjectHandlerTest.java
package com.tx.outsourcingmis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MyBatis Plus元对象处理器测试")
class MyMetaObjectHandlerTest {

    @Test
    @DisplayName("处理器实例创建")
    void testInstance() {
        MyMetaObjectHandler handler = new MyMetaObjectHandler();
        assertThat(handler).isNotNull();
    }
}