package com.blog.dto;

import lombok.Data;

/**
 * 聊天消息DTO
 */
@Data
public class ChatMessageDTO {
    
    private Long userId;
    
    private String content;
}
