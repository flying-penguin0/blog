package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 请求 DTO
 * 
 * @author Blog System
 */
@Data
public class AIRequestDTO {
    
    /**
     * 用户消息
     */
    @NotBlank(message = "消息不能为空")
    private String message;
    
    /**
     * 系统提示词（可选）
     */
    private String systemPrompt;
    
    /**
     * 文章内容（用于文章页 AI 助手）
     */
    private String articleContent;
    
    /**
     * 主题（用于生成大纲）
     */
    private String topic;
    
    /**
     * 大纲（用于生成内容）
     */
    private String outline;
    
    /**
     * 已有内容（用于续写）
     */
    private String content;
}
