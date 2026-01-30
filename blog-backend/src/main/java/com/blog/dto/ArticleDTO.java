package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * 文章请求 DTO
 * 
 * @author Blog System
 */
@Data
public class ArticleDTO {
    
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    private String summary;
    
    private String coverImage;
    
    private Long categoryId;
    
    private List<Long> tagIds;
    
    /**
     * 状态（draft:草稿 published:已发布）
     */
    private String status;
}
