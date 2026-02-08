package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.EmailService;
import com.blog.utils.CaptchaGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码控制器
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
@Tag(name = "验证码管理", description = "验证码相关接口")
public class CaptchaController {
    
    @Autowired
    private CaptchaGenerator captchaGenerator;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * 获取图片验证码
     */
    @GetMapping
    @Operation(summary = "获取图片验证码", description = "获取图片验证码")
    public Result<Map<String, String>> getCaptcha() {
        // 生成验证码key
        String captchaKey = UUID.randomUUID().toString();
        
        // 生成验证码图片（base64格式）
        String imageBase64 = captchaGenerator.getCaptchaImage(captchaKey);
        
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", "data:image/png;base64," + imageBase64);
        
        return Result.success(result);
    }
    
    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码")
    public Result<String> sendEmailCode(@RequestParam String username, @RequestParam String email) {
        emailService.sendVerificationCode(username, email);
        return Result.success("验证码已发送，请查收邮件");
    }
}
