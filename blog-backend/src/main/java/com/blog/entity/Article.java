package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体类
 * 
 * @author Blog System
 */
@Data
@TableName("article")
public class Article implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 作者ID
     */
    private Long userId;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章内容（Markdown）
     */
    private String content;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 封面图片URL
     */
    private String coverImage;
    
    /**
     * 浏览量
     */
    private Integer viewCount;

    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 状态（draft:草稿 published:已发布）
     */
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
}
