package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ChatRoomMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天室消息Mapper
 */
@Mapper
public interface ChatRoomMessageMapper extends BaseMapper<ChatRoomMessage> {
}
