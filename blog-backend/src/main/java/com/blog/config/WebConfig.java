package com.blog.config;

import com.blog.interceptor.JwtInterceptor;
import com.blog.interceptor.OptionalJwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 * 
 * @author Blog System
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Autowired
    private OptionalJwtInterceptor optionalJwtInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 可选的 JWT 拦截器 - 用于需要支持游客访问但也需要识别登录用户的接口
        registry.addInterceptor(optionalJwtInterceptor)
                .addPathPatterns("/comment/article/**")
                .order(1);
        
        // 必需的 JWT 拦截器 - 用于需要强制登录的接口
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/*/stats",  // 用户统计信息（文章数、浏览量、评论数）
                        "/captcha",  // 图片验证码接口
                        "/captcha/**",  // 邮箱验证码等其他验证码接口
                        "/article/list",
                        "/article/hot",
                        "/category/**",
                        "/tag/**",
                        "/comment/article/**",
                        "/message",
                        "/announcement/latest",  // 只排除前台获取最新公告的接口
                        "/chatroom/history",  // 聊天室历史消息
                        "/ws/**",  // WebSocket连接
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/druid/**",
                        "/error"
                )
                .order(2);
    }
}
