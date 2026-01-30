package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 VO
 * 
 * @author Blog System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    
    /**
     * Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserVO user;
}
