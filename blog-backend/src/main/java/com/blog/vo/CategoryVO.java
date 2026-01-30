package com.blog.vo;

import lombok.Data;

/**
 * 分类响应 VO
 * 
 * @author Blog System
 */
@Data
public class CategoryVO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private Integer articleCount;
}
