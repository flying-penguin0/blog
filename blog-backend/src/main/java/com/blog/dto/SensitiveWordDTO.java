package com.blog.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 敏感词 DTO
 * 
 * @author Blog System
 */
@Data
public class SensitiveWordDTO {
    
    /**
     * 敏感词
     */
    private String word;
    
    /**
     * 状态（1:启用 0:禁用）
     */
    private Integer status;
}
