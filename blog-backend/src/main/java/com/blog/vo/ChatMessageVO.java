package com.blog.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
public class ChatMessageVO {
    
    private Long id;
    
    private Long userId;
    
    // 以下字段通过关联查询user表获取，不存储在chatroom_message表中
    private String username;
    
    private String nickname;
    
    private String avatar;
    
    private String content;
    
    private String status; // approved-已通过, pending-待审核
    
    private LocalDateTime createTime;
}
