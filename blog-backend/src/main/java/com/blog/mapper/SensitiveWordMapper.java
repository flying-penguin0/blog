package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感词 Mapper
 * 
 * @author Blog System
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
}
