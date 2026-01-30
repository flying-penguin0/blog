package com.blog.interceptor;

import com.blog.common.ResultCode;
import com.blog.exception.BusinessException;
import com.blog.utils.JwtUtil;
import com.blog.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * 
 * @author Blog System
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.out.println("=== JWT 拦截器被调用 ===");
        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("请求方法: " + request.getMethod());
        
        // OPTIONS 请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            System.out.println("OPTIONS 请求，直接放行");
            return true;
        }
        
        // GET 请求访问文章详情不需要认证
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equals(method) && uri.matches(".*/article/\\d+$")) {
            System.out.println("GET 请求访问文章详情，直接放行");
            return true;
        }
        
        // 获取 Token
        String token = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + token);
        
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("Token 不存在或格式错误");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        token = token.substring(7);
        System.out.println("提取的 Token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("Token 验证失败");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        // 获取用户 ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        // 打印调试信息
        System.out.println("=== JWT 拦截器 ===");
        System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "...");
        System.out.println("用户ID: " + userId);
        System.out.println("用户名: " + username);
        System.out.println("角色: " + role);
        
        // 检查 Redis 中的 Token 是否存在
        String tokenKey = "token:" + userId;
        if (!redisUtil.hasKey(tokenKey)) {
            System.out.println("Redis 中不存在该 Token");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        // 将用户信息存入请求属性
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        
        System.out.println("JWT 拦截器执行完成，已设置 request 属性");
        
        return true;
    }
}
