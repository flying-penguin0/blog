package com.blog.dto;

import lombok.Data;

/**
 * 标签 DTO
 * 
 * @author Blog System
 */
@Data
public class TagDTO {
    
    private String name;
    
    private Long categoryId;
}
