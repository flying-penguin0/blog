package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 * 
 * @author Blog System
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
