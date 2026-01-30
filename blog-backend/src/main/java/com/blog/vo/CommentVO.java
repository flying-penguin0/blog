package com.blog.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应 VO
 * 
 * @author Blog System
 */
@Data
public class CommentVO {
    
    private Long id;
    
    private Long articleId;
    
    private Long userId;
    
    private Long parentId;
    
    private String content;
    
    private Integer likeCount;
    
    private Boolean isLiked;
    
    private String status;
    
    private LocalDateTime createTime;
    
    /**
     * 评论用户信息
     */
    private String username;
    private String nickname;
    private String avatar;
    
    /**
     * 父评论信息（二级评论时使用）
     */
    private String parentUsername;
    
    /**
     * 文章标题（管理员列表使用）
     */
    private String articleTitle;
    
    /**
     * 子评论列表（一级评论时使用）
     */
    private List<CommentVO> replies;
}
