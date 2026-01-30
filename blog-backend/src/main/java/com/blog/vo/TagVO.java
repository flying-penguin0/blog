package com.blog.vo;

import lombok.Data;

/**
 * 标签响应 VO
 * 
 * @author Blog System
 */
@Data
public class TagVO {
    
    private Long id;
    
    private String name;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Integer articleCount;
}
