package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 博客系统启动类
 * 
 * @author Blog System
 * @since 2026-01-28
 */
@SpringBootApplication
@MapperScan("com.blog.mapper")
@EnableAsync
@EnableScheduling
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
        System.out.println("""
            
            ========================================
            博客系统启动成功！
            API 文档地址: http://localhost:8080/api/swagger-ui.html
            Druid 监控: http://localhost:8080/api/druid/
            ========================================
            """);
    }
}
