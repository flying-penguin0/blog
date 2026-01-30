package com.blog.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息 VO
 * 
 * @author Blog System
 */
@Data
public class UserVO {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private String avatar;
    
    private String nickname;
    
    private String signature;
    
    private String role;
    
    private Integer status;
    
    private LocalDateTime createTime;
}
