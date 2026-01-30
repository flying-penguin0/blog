package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞实体
 * 
 * @author Blog System
 */
@Data
@TableName("comment_like")
public class CommentLike implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 评论ID
     */
    private Long commentId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 点赞时间
     */
    private LocalDateTime createTime;
}
