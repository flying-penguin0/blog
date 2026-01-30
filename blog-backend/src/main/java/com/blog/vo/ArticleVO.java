package com.blog.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章响应 VO
 * 
 * @author Blog System
 */
@Data
public class ArticleVO {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private String summary;
    
    private String coverImage;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer commentCount;
    
    private Integer rank; // 热门文章排名
    
    private String status;
    
    private Integer isTop;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime publishTime;
    
    /**
     * 作者信息
     */
    private Long userId;
    private String authorName;
    private String authorAvatar;
    
    /**
     * 分类信息
     */
    private Long categoryId;
    private String categoryName;
    
    /**
     * 标签列表
     */
    private List<TagVO> tags;
}
