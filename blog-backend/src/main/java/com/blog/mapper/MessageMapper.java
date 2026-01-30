package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留言 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
