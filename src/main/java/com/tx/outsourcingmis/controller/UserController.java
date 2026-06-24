package com.tx.outsourcingmis.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tx.outsourcingmis.annotation.RequirePermission;
import com.tx.outsourcingmis.common.ResultVO;
import com.tx.outsourcingmis.dto.LoginRequest;
import com.tx.outsourcingmis.dto.LoginResponse;
import com.tx.outsourcingmis.entity.User;
import com.tx.outsourcingmis.mapper.UserMapper;
import com.tx.outsourcingmis.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * <p>提供用户认证和用户信息管理接口：
 * <ul>
 *   <li>用户注册</li>
 *   <li>用户登录（返回 JWT Token）</li>
 *   <li>用户登出</li>
 *   <li>获取当前登录用户信息</li>
 *   <li>根据用户名查询用户信息</li>
 *   <li>分页查询用户列表</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理模块", description = "用户注册、登录、退出、信息查询")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param user 用户信息（用户名、密码、真实姓名、邮箱、手机号等）
     * @return 操作结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ResultVO<Void> register(@Valid @RequestBody User user) {
        userService.register(user);
        return ResultVO.success();
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求（用户名、密码）
     * @return 登录响应（含 JWT Token、用户信息、角色、权限列表）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResultVO<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResultVO.success(userService.login(loginRequest));
    }

    /**
     * 用户登出
     *
     * <p>清除 Redis 中的登录缓存，使 Token 失效。
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ResultVO<Void> logout() {
        userService.logout();
        return ResultVO.success();
    }

    /**
     * 获取当前登录用户信息
     *
     * <p>从 ThreadLocal 获取当前用户 ID，再查询用户详情。
     *
     * @return 当前用户信息（密码字段已置空）
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息")
    public ResultVO<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        user.setPassword(null);
        return ResultVO.success(user);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息（密码字段已置空），不存在时返回 null
     */
    @GetMapping("/info/{username}")
    @Operation(summary = "根据用户名获取用户信息（需要user:view权限）")
    @RequirePermission("user:view")
    public ResultVO<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            user.setPassword(null);
        }
        return ResultVO.success(user);
    }

    /**
     * 分页查询用户列表（用于前端展示）
     *
     * @param pageNum 页码，默认 1
     * @param pageSize 每页大小，默认 10
     * @return 用户列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表")
    @RequirePermission("user:view")
    public ResultVO<PageInfo<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectList(null);
        // 隐藏密码
        users.forEach(user -> user.setPassword(null));
        return ResultVO.success(new PageInfo<>(users));
    }
}