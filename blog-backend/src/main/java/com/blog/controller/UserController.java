package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.service.UserService;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 
 * @author Blog System
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }
    
    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success("登录成功", loginVO);
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }
    
    /**
     * 更新当前用户信息
     */
    @Operation(summary = "更新当前用户信息")
    @PutMapping("/update")
    public Result<Void> updateCurrentUser(@RequestBody UserVO userVO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateCurrentUser(userId, userVO);
        return Result.success("更新成功", null);
    }
    
    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody com.blog.dto.ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, dto);
        return Result.success("密码修改成功", null);
    }
    
    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 从 Redis 中删除 Token
        // redisUtil.delete("token:" + userId);
        return Result.success("退出成功", null);
    }
    
    /**
     * 获取所有用户列表（管理员）
     */
    @Operation(summary = "获取所有用户列表")
    @GetMapping("/list")
    public Result<com.blog.common.PageResult<UserVO>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        com.blog.common.PageResult<UserVO> result = userService.getAllUsers(username, role, page, size);
        return Result.success(result);
    }
    
    /**
     * 更新用户信息（管理员）
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserVO userVO) {
        userService.updateUser(id, userVO);
        return Result.success("更新成功", null);
    }
    
    /**
     * 切换用户状态（管理员）
     */
    @Operation(summary = "切换用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return Result.success("操作成功", null);
    }
    
    /**
     * 删除用户（管理员）
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }
    
    /**
     * 获取用户统计信息
     */
    @Operation(summary = "获取用户统计信息")
    @GetMapping("/{id}/stats")
    public Result<java.util.Map<String, Object>> getUserStats(@PathVariable Long id) {
        java.util.Map<String, Object> stats = userService.getUserStats(id);
        return Result.success(stats);
    }
}
