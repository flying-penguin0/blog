package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.Result;
import com.blog.dto.ArticleDTO;
import com.blog.service.ArticleService;
import com.blog.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 * 
 * @author Blog System
 */
@Tag(name = "文章管理")
@RestController
@RequestMapping("/article")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 创建文章
     */
    @Operation(summary = "创建文章")
    @PostMapping
    public Result<Void> createArticle(@Valid @RequestBody ArticleDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        articleService.createArticle(dto, userId);
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新文章
     */
    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, 
                                      @Valid @RequestBody ArticleDTO dto,
                                      HttpServletRequest request) {
        // 调试日志
        System.out.println("=== ArticleController.updateArticle ===");
        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("请求方法: " + request.getMethod());
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));
        
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        System.out.println("从 request 获取的 userId: " + userId);
        System.out.println("从 request 获取的 role: " + role);
        
        articleService.updateArticle(id, dto, userId, role);
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除文章
     */
    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        articleService.deleteArticle(id, userId, role);
        return Result.success("删除成功", null);
    }
    
    /**
     * 获取文章详情
     */
    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getArticleDetail(id);
        // 增加浏览量
        articleService.incrementViewCount(id);
        return Result.success(articleVO);
    }
    
    /**
     * 分页查询文章列表
     */
    @Operation(summary = "分页查询文章列表")
    @GetMapping("/list")
    public Result<Page<ArticleVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        Page<ArticleVO> result = articleService.getArticleList(page, size, categoryId, tagId, keyword, userId, status);
        return Result.success(result);
    }
    
    /**
     * 获取热门文章
     */
    @Operation(summary = "获取热门文章")
    @GetMapping("/hot")
    public Result<List<ArticleVO>> getHotArticles(@RequestParam(defaultValue = "10") Integer limit) {
        List<ArticleVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }
}
