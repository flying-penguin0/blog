package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.CommentDTO;
import com.blog.service.CommentService;
import com.blog.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 * 
 * @author Blog System
 */
@Tag(name = "评论管理")
@RestController
@RequestMapping("/comment")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 发表评论
     */
    @Operation(summary = "发表评论")
    @PostMapping
    public Result<String> createComment(@Valid @RequestBody CommentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String status = commentService.createComment(dto, userId);
        
        if ("pending".equals(status)) {
            return Result.success("评论已提交，包含敏感词需要审核", status);
        } else {
            return Result.success("评论成功", status);
        }
    }
    
    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        commentService.deleteComment(id, userId, role);
        return Result.success("删除成功", null);
    }
    
    /**
     * 获取文章评论列表
     */
    @Operation(summary = "获取文章评论列表")
    @GetMapping("/article/{articleId}")
    public Result<List<CommentVO>> getArticleComments(
            @PathVariable Long articleId,
            HttpServletRequest request) {
        // 尝试获取当前用户ID（游客为null）
        Long currentUserId = null;
        try {
            currentUserId = (Long) request.getAttribute("userId");
        } catch (Exception e) {
            // 游客访问，currentUserId 保持为 null
        }
        
        List<CommentVO> comments = commentService.getArticleComments(articleId, currentUserId);
        return Result.success(comments);
    }
    
    /**
     * 点赞/取消点赞评论
     */
    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/{id}/like")
    public Result<Void> toggleLike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        commentService.toggleLike(id, userId);
        return Result.success("操作成功", null);
    }
    
    /**
     * 审核评论（管理员）
     */
    @Operation(summary = "审核评论")
    @PutMapping("/{id}/audit")
    public Result<Void> auditComment(@PathVariable Long id, @RequestParam String status) {
        commentService.auditComment(id, status);
        return Result.success("审核成功", null);
    }
    
    /**
     * 批量审核评论（管理员）
     */
    @Operation(summary = "批量审核评论")
    @PutMapping("/batch/audit")
    public Result<Void> batchAuditComments(@RequestBody List<Long> ids, @RequestParam String status) {
        commentService.batchAuditComments(ids, status);
        return Result.success("批量审核成功", null);
    }
    
    /**
     * 批量删除评论（管理员）
     */
    @Operation(summary = "批量删除评论")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteComments(@RequestBody List<Long> ids) {
        commentService.batchDeleteComments(ids);
        return Result.success("批量删除成功", null);
    }
    
    /**
     * 获取所有评论列表（管理员）
     */
    @Operation(summary = "获取所有评论列表")
    @GetMapping("/list")
    public Result<PageResult<CommentVO>> getAllComments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String articleTitle,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId) {
        PageResult<CommentVO> result = commentService.getAllComments(keyword, status, content, articleTitle, page, size, userId);
        return Result.success(result);
    }
}
