// src/test/java/com/tx/outsourcingmis/entity/EntityTests.java
package com.tx.outsourcingmis.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("实体类测试")
class EntityTests {

    @Test
    @DisplayName("User实体测试")
    void testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("123456");
        user.setRealName("测试用户");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setRole("EMPLOYEE");
        user.setStatus(1);
        user.setLeaderId(100L);
        user.setUpdateTime(LocalDateTime.now());

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("123456");
        assertThat(user.getRealName()).isEqualTo("测试用户");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPhone()).isEqualTo("13800138000");
        assertThat(user.getRole()).isEqualTo("EMPLOYEE");
        assertThat(user.getStatus()).isEqualTo(1);
        assertThat(user.getLeaderId()).isEqualTo(100L);
        assertThat(user.getUpdateTime()).isNotNull();
    }

    @Test
    @DisplayName("JobApplication实体测试")
    void testJobApplication() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setApplicantId(100L);
        app.setApproverId(200L);
        app.setReason("申请加入项目组");
        app.setStatus(0);
        app.setRejectReason(null);
        app.setApplyTime(LocalDateTime.now());
        app.setApproveTime(null);
        app.setRemark("有3年经验");

        assertThat(app.getId()).isEqualTo(1L);
        assertThat(app.getApplicantId()).isEqualTo(100L);
        assertThat(app.getApproverId()).isEqualTo(200L);
        assertThat(app.getReason()).isEqualTo("申请加入项目组");
        assertThat(app.getStatus()).isEqualTo(0);
        assertThat(app.getRemark()).isEqualTo("有3年经验");
        assertThat(app.getApplyTime()).isNotNull();
    }

    @Test
    @DisplayName("Performance实体测试")
    void testPerformance() {
        Performance perf = new Performance();
        perf.setId(1L);
        perf.setUserId(100L);
        perf.setEvaluatorId(200L);
        perf.setGrade("B");
        perf.setComment("工作表现良好");
        perf.setEvaluateYear(2026);
        perf.setEvaluateMonth(6);
        perf.setStatus(0);
        perf.setCreateTime(LocalDateTime.now());
        perf.setUpdateTime(LocalDateTime.now());

        assertThat(perf.getId()).isEqualTo(1L);
        assertThat(perf.getUserId()).isEqualTo(100L);
        assertThat(perf.getEvaluatorId()).isEqualTo(200L);
        assertThat(perf.getGrade()).isEqualTo("B");
        assertThat(perf.getComment()).isEqualTo("工作表现良好");
        assertThat(perf.getEvaluateYear()).isEqualTo(2026);
        assertThat(perf.getEvaluateMonth()).isEqualTo(6);
        assertThat(perf.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("Role实体测试")
    void testRole() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName("ADMIN");
        role.setDescription("系统管理员");

        assertThat(role.getId()).isEqualTo(1L);
        assertThat(role.getRoleName()).isEqualTo("ADMIN");
        assertThat(role.getDescription()).isEqualTo("系统管理员");
    }

    @Test
    @DisplayName("Permission实体测试")
    void testPermission() {
        Permission perm = new Permission();
        perm.setId(1L);
        perm.setPermissionName("用户查看");
        perm.setPermissionCode("user:view");

        assertThat(perm.getId()).isEqualTo(1L);
        assertThat(perm.getPermissionName()).isEqualTo("用户查看");
        assertThat(perm.getPermissionCode()).isEqualTo("user:view");
    }

    @Test
    @DisplayName("UserRole实体测试")
    void testUserRole() {
        UserRole userRole = new UserRole();
        userRole.setId(1L);
        userRole.setUserId(100L);
        userRole.setRoleId(1L);

        assertThat(userRole.getId()).isEqualTo(1L);
        assertThat(userRole.getUserId()).isEqualTo(100L);
        assertThat(userRole.getRoleId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("RolePermission实体测试")
    void testRolePermission() {
        RolePermission rp = new RolePermission();
        rp.setId(1L);
        rp.setRoleId(1L);
        rp.setPermissionId(1L);

        assertThat(rp.getId()).isEqualTo(1L);
        assertThat(rp.getRoleId()).isEqualTo(1L);
        assertThat(rp.getPermissionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("OperationLog实体测试")
    void testOperationLog() {
        OperationLog log = new OperationLog();
        log.setId(1L);
        log.setUserId(100L);
        log.setUsername("testuser");
        log.setOperation("登录");
        log.setMethod("POST");
        log.setParams("{\"username\":\"test\"}");
        log.setIp("127.0.0.1");
        log.setDuration(100L);
        log.setCreateTime(LocalDateTime.now());

        assertThat(log.getId()).isEqualTo(1L);
        assertThat(log.getUserId()).isEqualTo(100L);
        assertThat(log.getUsername()).isEqualTo("testuser");
        assertThat(log.getOperation()).isEqualTo("登录");
        assertThat(log.getDuration()).isEqualTo(100L);
    }
}