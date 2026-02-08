package com.blog.service.impl;

import com.blog.exception.BusinessException;
import com.blog.service.EmailService;
import com.blog.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类
 * 
 * @author Blog System
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Value("${spring.mail.username}")
    private String from;
    
    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String EMAIL_SEND_LIMIT_PREFIX = "email:limit:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_LIMIT_SECONDS = 60;
    
    @Override
    public String sendVerificationCode(String username, String email) {
        // 检查发送频率限制
        String limitKey = EMAIL_SEND_LIMIT_PREFIX + username + ":" + email;
        if (redisUtil.hasKey(limitKey)) {
            Long ttl = redisUtil.getExpire(limitKey);
            throw new BusinessException("验证码发送过于频繁，请" + ttl + "秒后再试");
        }
        
        // 生成6位随机验证码
        String code = generateCode();
        
        // 将验证码存入Redis，使用用户名+邮箱作为key
        String codeKey = EMAIL_CODE_PREFIX + username + ":" + email;
        redisUtil.set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 设置发送频率限制
        redisUtil.set(limitKey, "1", SEND_LIMIT_SECONDS, TimeUnit.SECONDS);
        
        // 异步发送邮件（不等待发送完成）
        sendEmailAsync(email, code);
        
        log.info("邮箱验证码已生成: username={}, email={}", username, email);
        return code;
    }
    
    @Override
    public boolean verifyCode(String username, String email, String code) {
        String codeKey = EMAIL_CODE_PREFIX + username + ":" + email;
        String cachedCode = (String) redisUtil.get(codeKey);
        if (cachedCode == null) {
            return false;
        }
        
        boolean isValid = cachedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码
            redisUtil.delete(codeKey);
            // 同时删除发送限制，允许用户重新注册
            String limitKey = EMAIL_SEND_LIMIT_PREFIX + username + ":" + email;
            redisUtil.delete(limitKey);
        }
        
        return isValid;
    }
    
    /**
     * 异步发送邮件
     */
    @Async
    public void sendEmailAsync(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("【个人知识博客】邮箱验证码");
            message.setText(String.format(
                "您的验证码是：%s\n\n" +
                "验证码有效期为%d分钟，请勿泄露给他人。\n\n" +
                "如果这不是您的操作，请忽略此邮件。\n\n" +
                "——个人知识博客系统",
                code, CODE_EXPIRE_MINUTES
            ));
            
            mailSender.send(message);
            log.info("邮箱验证码发送成功: {}", email);
        } catch (Exception e) {
            log.error("邮箱验证码发送失败: {}", email, e);
            // 异步发送失败不抛出异常，避免影响用户体验
        }
    }
    
    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
