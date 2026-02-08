package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天室消息实体
 */
@Data
@TableName("chatroom_message")
public class ChatRoomMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String content;
    
    private String status; // approved-已通过, pending-待审核, rejected-已拒绝
    
    private LocalDateTime createTime;
}
