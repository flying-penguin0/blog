package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.dto.ChangePasswordDTO;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.entity.User;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;

/**
 * 用户服务接口
 * 
 * @author Blog System
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    void register(RegisterDTO dto);
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);
    
    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);
    
    /**
     * 更新当前用户信息
     */
    void updateCurrentUser(Long userId, UserVO userVO);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordDTO dto);
    
    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
    
    /**
     * 获取所有用户列表（管理员）
     */
    PageResult<UserVO> getAllUsers(String username, String role, Integer page, Integer size);
    
    /**
     * 更新用户信息（管理员）
     */
    void updateUser(Long id, UserVO userVO);
    
    /**
     * 切换用户状态（管理员）
     */
    void toggleUserStatus(Long id);
    
    /**
     * 删除用户（管理员）
     */
    void deleteUser(Long id);
    
    /**
     * 获取用户统计信息
     */
    java.util.Map<String, Object> getUserStats(Long userId);
}
