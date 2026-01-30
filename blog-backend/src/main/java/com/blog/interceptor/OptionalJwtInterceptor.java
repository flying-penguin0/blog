package com.blog.interceptor;

import com.blog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选的 JWT 拦截器
 * 如果请求中包含有效的 JWT token，则解析并设置用户信息
 * 如果没有 token 或 token 无效，则不设置用户信息，但允许请求继续
 * 
 * @author Blog System
 */
@Component
public class OptionalJwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 获取 Token
        String token = request.getHeader("Authorization");
        
        // 如果没有 token，直接放行（游客访问）
        if (token == null || !token.startsWith("Bearer ")) {
            return true;
        }
        
        try {
            token = token.substring(7);
            
            // 验证 Token
            if (jwtUtil.validateToken(token)) {
                // 获取用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                // 将用户信息存入请求属性
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);
                
                System.out.println("=== Optional JWT 拦截器 ===");
                System.out.println("用户ID: " + userId);
                System.out.println("用户名: " + username);
                System.out.println("角色: " + role);
            }
        } catch (Exception e) {
            // Token 解析失败，忽略错误，继续处理请求（作为游客）
            System.out.println("Optional JWT 解析失败，作为游客处理: " + e.getMessage());
        }
        
        return true;
    }
}
