// src/test/java/com/tx/outsourcingmis/common/ResultVOTest.java
package com.tx.outsourcingmis.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("通用响应对象测试")
class ResultVOTest {

    @Test
    @DisplayName("成功响应 - 带数据")
    void success_WithData() {
        String testData = "test data";
        ResultVO<String> result = ResultVO.success(testData);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo(testData);
    }

    @Test
    @DisplayName("成功响应 - 无数据")
    void success_WithoutData() {
        ResultVO<Void> result = ResultVO.success();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("success");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("错误响应 - 默认500")
    void error_Default() {
        ResultVO<Void> result = ResultVO.error("发生错误");

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("发生错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("错误响应 - 自定义状态码")
    void error_WithCode() {
        ResultVO<Void> result = ResultVO.error(400, "参数错误");

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("参数错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("错误响应 - 401未授权")
    void error_Unauthorized() {
        ResultVO<Void> result = ResultVO.error(401, "请先登录");

        assertThat(result.getCode()).isEqualTo(401);
        assertThat(result.getMsg()).isEqualTo("请先登录");
    }

    @Test
    @DisplayName("错误响应 - 403无权限")
    void error_Forbidden() {
        ResultVO<Void> result = ResultVO.error(403, "无权限访问");

        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMsg()).isEqualTo("无权限访问");
    }

    @Test
    @DisplayName("链式调用 - setter方法")
    void setterMethods() {
        ResultVO<String> result = new ResultVO<>();
        result.setCode(201);
        result.setMsg("created");
        result.setData("resource id: 1");

        assertThat(result.getCode()).isEqualTo(201);
        assertThat(result.getMsg()).isEqualTo("created");
        assertThat(result.getData()).isEqualTo("resource id: 1");
    }
}