package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.entity.Message;
import com.blog.entity.User;
import com.blog.mapper.MessageMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.MessageService;
import com.blog.utils.SensitiveWordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 留言服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;
    
    @Override
    public void createMessage(Message message) {
        // 检测敏感词
        boolean hasSensitiveWord = sensitiveWordUtil.containsSensitiveWord(message.getContent());
        
        if (hasSensitiveWord) {
            // 有敏感词，设置为待审核状态
            message.setStatus("pending");
        } else {
            // 无敏感词，直接通过
            message.setStatus("approved");
        }
        
        messageMapper.insert(message);
    }
    
    @Override
    public PageResult<Message> getMessageList(Integer page, Integer size, String status) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Message::getStatus, status);
        }
        
        wrapper.orderByDesc(Message::getCreateTime);
        Page<Message> result = messageMapper.selectPage(pageParam, wrapper);
        
        // 填充用户信息
        List<Message> messages = result.getRecords();
        if (!messages.isEmpty()) {
            List<Long> userIds = messages.stream()
                    .map(Message::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            
            messages.forEach(message -> {
                User user = userMap.get(message.getUserId());
                if (user != null) {
                    message.setUsername(user.getUsername());
                    message.setNickname(user.getNickname());
                    message.setAvatar(user.getAvatar());
                }
            });
        }
        
        return new PageResult<>(messages, result.getTotal(), page, size);
    }
    
    @Override
    public void deleteMessage(Long id) {
        messageMapper.deleteById(id);
    }
    
    @Override
    public void auditMessage(Long id, String status) {
        Message message = new Message();
        message.setId(id);
        message.setStatus(status);
        messageMapper.updateById(message);
    }
    
    @Override
    public PageResult<Message> getMyMessages(Long userId, Integer page, Integer size, String content, String status) {
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        
        // 添加搜索条件
        if (content != null && !content.trim().isEmpty()) {
            wrapper.like(Message::getContent, content);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Message::getStatus, status);
        }
        
        wrapper.orderByDesc(Message::getCreateTime);
        
        Page<Message> result = messageMapper.selectPage(pageParam, wrapper);
        
        // 填充用户信息
        List<Message> messages = result.getRecords();
        if (!messages.isEmpty()) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                messages.forEach(message -> {
                    message.setUsername(user.getUsername());
                    message.setNickname(user.getNickname());
                    message.setAvatar(user.getAvatar());
                });
            }
        }
        
        return new PageResult<>(messages, result.getTotal(), page, size);
    }
}
