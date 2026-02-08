package com.blog.service;

import com.blog.dto.ChatMessageDTO;
import com.blog.vo.ChatMessageVO;
import com.blog.vo.UserVO;

import java.util.List;

/**
 * 聊天室服务接口
 */
public interface ChatRoomService {
    
    /**
     * 保存消息
     */
    ChatMessageVO saveMessage(Long userId, ChatMessageDTO dto);
    
    /**
     * 获取最近的消息
     */
    List<ChatMessageVO> getRecentMessages(Integer limit);
    
    /**
     * 获取在线用户列表
     */
    List<UserVO> getOnlineUsers();
    
    /**
     * 用户上线
     */
    void userOnline(Long userId);
    
    /**
     * 用户下线
     */
    void userOffline(Long userId);
    
    /**
     * 获取所有消息（管理员）
     */
    List<ChatMessageVO> getAllMessages(Integer page, Integer size);
    
    /**
     * 删除消息（管理员）
     */
    void deleteMessage(Long id);
    
    /**
     * 审核通过消息（管理员）
     */
    void approveMessage(Long id);
    
    /**
     * 拒绝消息（管理员）
     */
    void rejectMessage(Long id);
    
    /**
     * 获取待审核消息数量
     */
    Long getPendingCount();
    
    /**
     * 获取我的聊天记录
     */
    com.blog.common.PageResult<ChatMessageVO> getMyChatMessages(Long userId, Integer page, Integer size, String content, String status);
}
