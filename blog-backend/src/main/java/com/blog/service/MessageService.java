package com.blog.service;

import com.blog.common.PageResult;
import com.blog.entity.Message;

/**
 * 留言服务接口
 */
public interface MessageService {
    
    /**
     * 创建留言
     */
    void createMessage(Message message);
    
    /**
     * 获取留言列表（分页）
     */
    PageResult<Message> getMessageList(Integer page, Integer size, String status);
    
    /**
     * 获取我的留言列表（分页）
     */
    PageResult<Message> getMyMessages(Long userId, Integer page, Integer size, String content, String status);
    
    /**
     * 删除留言
     */
    void deleteMessage(Long id);
    
    /**
     * 审核留言
     */
    void auditMessage(Long id, String status);
}
