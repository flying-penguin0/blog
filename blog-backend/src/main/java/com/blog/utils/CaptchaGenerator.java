package com.blog.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码工具类
 */
@Slf4j
@Component
public class CaptchaGenerator {
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_TIME = 1; // 5分钟过期
    
    /**
     * 获取验证码图片
     * @param captchaKey 验证码key
     * @return 验证码图片的base64编码
     */
    public String getCaptchaImage(String captchaKey) {
        // 生成验证码图片
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(130, 48, 4, 20);
        RandomGenerator generator = new RandomGenerator("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", 4);
        lineCaptcha.setGenerator(generator);
        lineCaptcha.createCode();
        String code = lineCaptcha.getCode();
        // 存储到Redis
        redisUtil.set(CAPTCHA_PREFIX + captchaKey, code, CAPTCHA_EXPIRE_TIME * 60, TimeUnit.SECONDS);
        
        log.info("生成验证码图片: key={}, code={}", captchaKey, code);
        
        // 返回base64编码的图片
        return lineCaptcha.getImageBase64();
    }
    
    /**
     * 验证验证码
     * @param captchaKey 验证码key
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean verifyCaptcha(String captchaKey, String code) {
        if (captchaKey == null || code == null) {
            return false;
        }
        
        String redisKey = CAPTCHA_PREFIX + captchaKey;
        String cachedCode = (String) redisUtil.get(redisKey);
        
        if (cachedCode == null) {
            log.warn("验证码已过期或不存在: key={}", captchaKey);
            return false;
        }
        
        // 验证后删除验证码（一次性使用）
        redisUtil.delete(redisKey);
        
        boolean result = cachedCode.equalsIgnoreCase(code.trim());
        log.info("验证码校验: key={}, input={}, cached={}, result={}", 
                captchaKey, code, cachedCode, result);
        
        return result;
    }
}
