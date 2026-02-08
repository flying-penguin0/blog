package com.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.ChatMessageDTO;
import com.blog.entity.ChatRoomMessage;
import com.blog.entity.User;
import com.blog.mapper.ChatRoomMessageMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.ChatRoomService;
import com.blog.utils.RedisUtil;
import com.blog.utils.SensitiveWordUtil;
import com.blog.vo.ChatMessageVO;
import com.blog.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 聊天室服务实现
 */
@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    
    @Autowired
    private ChatRoomMessageMapper messageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;
    
    // 使用内存存储在线用户（简单方案）
    private static final Map<Long, Long> onlineUsers = new ConcurrentHashMap<>();
    
    @Override
    public ChatMessageVO saveMessage(Long userId, ChatMessageDTO dto) {
        // 检测敏感词
        boolean hasSensitiveWord = sensitiveWordUtil.containsSensitiveWord(dto.getContent());
        String content = dto.getContent();
        String status = "approved"; // 默认通过
        
        if (hasSensitiveWord) {
            // 包含敏感词，设置为待审核状态
            status = "pending";
        }
        
        // 创建消息
        ChatRoomMessage message = new ChatRoomMessage();
        message.setUserId(userId);
        message.setContent(content);
        message.setStatus(status);
        
        // 保存到数据库
        messageMapper.insert(message);
        
        // 转换为VO并关联用户信息
        ChatMessageVO vo = BeanUtil.copyProperties(message, ChatMessageVO.class);
        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        
        return vo;
    }
    
    @Override
    public List<ChatMessageVO> getRecentMessages(Integer limit) {
        LambdaQueryWrapper<ChatRoomMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMessage::getStatus, "approved"); // 只查询已通过的消息
        wrapper.orderByDesc(ChatRoomMessage::getCreateTime);
        wrapper.last("LIMIT " + limit);
        
        List<ChatRoomMessage> messages = messageMapper.selectList(wrapper);
        
        // 反转列表，使最新的消息在最后
        List<ChatRoomMessage> reversedMessages = new ArrayList<>(messages);
        Collections.reverse(reversedMessages);
        
        // 转换为VO并关联用户信息
        return reversedMessages.stream()
                .map(msg -> {
                    ChatMessageVO vo = BeanUtil.copyProperties(msg, ChatMessageVO.class);
                    // 关联查询用户信息
                    User user = userMapper.selectById(msg.getUserId());
                    if (user != null) {
                        vo.setUsername(user.getUsername());
                        vo.setNickname(user.getNickname());
                        vo.setAvatar(user.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserVO> getOnlineUsers() {
        // 从内存获取在线用户ID列表
        Set<Long> userIds = onlineUsers.keySet();
        
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        
        return users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public void userOnline(Long userId) {
        onlineUsers.put(userId, System.currentTimeMillis());
    }
    
    @Override
    public void userOffline(Long userId) {
        onlineUsers.remove(userId);
    }
    
    @Override
    public List<ChatMessageVO> getAllMessages(Integer page, Integer size) {
        LambdaQueryWrapper<ChatRoomMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatRoomMessage::getCreateTime);
        
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        
        List<ChatRoomMessage> messages = messageMapper.selectList(wrapper);
        
        // 转换为VO并关联用户信息
        return messages.stream()
                .map(msg -> {
                    ChatMessageVO vo = BeanUtil.copyProperties(msg, ChatMessageVO.class);
                    // 关联查询用户信息
                    User user = userMapper.selectById(msg.getUserId());
                    if (user != null) {
                        vo.setUsername(user.getUsername());
                        vo.setNickname(user.getNickname());
                        vo.setAvatar(user.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteMessage(Long id) {
        messageMapper.deleteById(id);
    }
    
    @Override
    public void approveMessage(Long id) {
        ChatRoomMessage message = messageMapper.selectById(id);
        if (message != null) {
            message.setStatus("approved");
            messageMapper.updateById(message);
        }
    }
    
    @Override
    public void rejectMessage(Long id) {
        ChatRoomMessage message = messageMapper.selectById(id);
        if (message != null) {
            message.setStatus("rejected");
            messageMapper.updateById(message);
        }
    }
    
    @Override
    public Long getPendingCount() {
        LambdaQueryWrapper<ChatRoomMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMessage::getStatus, "pending");
        return messageMapper.selectCount(wrapper);
    }
    
    @Override
    public com.blog.common.PageResult<ChatMessageVO> getMyChatMessages(Long userId, Integer page, Integer size, String content, String status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ChatRoomMessage> pageParam = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        LambdaQueryWrapper<ChatRoomMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMessage::getUserId, userId);
        
        // 添加搜索条件
        if (content != null && !content.trim().isEmpty()) {
            wrapper.like(ChatRoomMessage::getContent, content);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(ChatRoomMessage::getStatus, status);
        }
        
        wrapper.orderByDesc(ChatRoomMessage::getCreateTime);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ChatRoomMessage> result = 
            messageMapper.selectPage(pageParam, wrapper);
        
        // 转换为VO并关联用户信息
        List<ChatMessageVO> voList = result.getRecords().stream()
                .map(msg -> {
                    ChatMessageVO vo = BeanUtil.copyProperties(msg, ChatMessageVO.class);
                    // 关联查询用户信息
                    User user = userMapper.selectById(msg.getUserId());
                    if (user != null) {
                        vo.setUsername(user.getUsername());
                        vo.setNickname(user.getNickname());
                        vo.setAvatar(user.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        return new com.blog.common.PageResult<>(voList, result.getTotal(), page, size);
    }

}
 
