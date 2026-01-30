package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论请求 DTO
 * 
 * @author Blog System
 */
@Data
public class CommentDTO {
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    /**
     * 父评论ID（一级评论为null）
     */
    private Long parentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
