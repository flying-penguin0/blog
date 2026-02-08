package com.blog.service;

/**
 * 邮件服务接口
 * 
 * @author Blog System
 */
public interface EmailService {
    
    /**
     * 发送邮箱验证码
     * 
     * @param username 用户名
     * @param email 邮箱地址
     * @return 验证码
     */
    String sendVerificationCode(String username, String email);
    
    /**
     * 验证邮箱验证码
     * 
     * @param username 用户名
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String username, String email, String code);
}
