package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.dto.CommentDTO;
import com.blog.entity.Comment;
import com.blog.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 * 
 * @author Blog System
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * 发表评论
     * @return 评论状态（approved 或 pending）
     */
    String createComment(CommentDTO dto, Long userId);
    
    /**
     * 删除评论
     */
    void deleteComment(Long id, Long userId, String role);
    
    /**
     * 获取文章评论列表（树形结构）
     */
    List<CommentVO> getArticleComments(Long articleId, Long currentUserId);
    
    /**
     * 审核评论（管理员）
     */
    void auditComment(Long id, String status);
    
    /**
     * 点赞/取消点赞评论
     */
    void toggleLike(Long commentId, Long userId);
    
    /**
     * 批量审核评论（管理员）
     */
    void batchAuditComments(List<Long> ids, String status);
    
    /**
     * 批量删除评论（管理员）
     */
    void batchDeleteComments(List<Long> ids);
    
    /**
     * 获取所有评论列表（管理员）
     */
    PageResult<CommentVO> getAllComments(String keyword, String status, String content, String articleTitle, Integer page, Integer size, Long userId);
}
