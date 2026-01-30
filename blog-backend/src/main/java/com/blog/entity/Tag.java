package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体类
 * 
 * @author Blog System
 */
@Data
@TableName("tag")
public class Tag implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 所属分类ID
     */
    private Long categoryId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
