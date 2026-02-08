package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.ChatMessageDTO;
import com.blog.service.ChatRoomService;
import com.blog.vo.ChatMessageVO;
import com.blog.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天室控制器
 */
@Tag(name = "聊天室管理")
@RestController
@RequestMapping("/chatroom")
public class ChatRoomController {
    
    @Autowired
    private ChatRoomService chatRoomService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 发送消息（通过WebSocket）
     */
    @MessageMapping("/chatroom/send")
    @SendTo("/topic/chatroom")
    public ChatMessageVO sendMessage(@Payload ChatMessageDTO dto) {
        // 注意：这里暂时无法获取userId，需要在前端发送时包含userId
        // 或者使用Spring Security的认证信息
        // 临时方案：从DTO中获取userId
        return chatRoomService.saveMessage(dto.getUserId(), dto);
    }
    
    /**
     * 获取历史消息
     */
    @Operation(summary = "获取历史消息")
    @GetMapping("/history")
    public Result<List<ChatMessageVO>> getHistory(@RequestParam(defaultValue = "50") Integer limit) {
        List<ChatMessageVO> messages = chatRoomService.getRecentMessages(limit);
        return Result.success(messages);
    }
    
    /**
     * 获取在线用户
     */
    @Operation(summary = "获取在线用户")
    @GetMapping("/online-users")
    public Result<List<UserVO>> getOnlineUsers() {
        List<UserVO> users = chatRoomService.getOnlineUsers();
        return Result.success(users);
    }
    
    /**
     * 用户上线
     */
    @Operation(summary = "用户上线")
    @PostMapping("/online")
    public Result<Void> userOnline(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        chatRoomService.userOnline(userId);
        
        // 广播用户上线消息
        messagingTemplate.convertAndSend("/topic/chatroom/status", "online:" + userId);
        
        return Result.success();
    }
    
    /**
     * 用户下线
     */
    @Operation(summary = "用户下线")
    @PostMapping("/offline")
    public Result<Void> userOffline(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        chatRoomService.userOffline(userId);
        
        // 广播用户下线消息
        messagingTemplate.convertAndSend("/topic/chatroom/status", "offline:" + userId);
        
        return Result.success();
    }
    
    /**
     * 获取所有消息（管理员）
     */
    @Operation(summary = "获取所有消息（管理员）")
    @GetMapping("/admin/messages")
    public Result<List<ChatMessageVO>> getAllMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(chatRoomService.getAllMessages(page, size));
    }
    
    /**
     * 删除消息（管理员）
     */
    @Operation(summary = "删除消息（管理员）")
    @DeleteMapping("/admin/messages/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        chatRoomService.deleteMessage(id);
        return Result.success();
    }
    
    /**
     * 审核通过消息（管理员）
     */
    @Operation(summary = "审核通过消息（管理员）")
    @PutMapping("/admin/messages/{id}/approve")
    public Result<Void> approveMessage(@PathVariable Long id) {
        chatRoomService.approveMessage(id);
        return Result.success();
    }
    
    /**
     * 拒绝消息（管理员）
     */
    @Operation(summary = "拒绝消息（管理员）")
    @PutMapping("/admin/messages/{id}/reject")
    public Result<Void> rejectMessage(@PathVariable Long id) {
        chatRoomService.rejectMessage(id);
        return Result.success();
    }
    
    /**
     * 获取待审核消息数量
     */
    @Operation(summary = "获取待审核消息数量")
    @GetMapping("/admin/pending-count")
    public Result<Long> getPendingCount() {
        return Result.success(chatRoomService.getPendingCount());
    }
    
    /**
     * 获取我的聊天记录
     */
    @Operation(summary = "获取我的聊天记录")
    @GetMapping("/my-messages")
    public Result<com.blog.common.PageResult<ChatMessageVO>> getMyChatMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(chatRoomService.getMyChatMessages(userId, page, size, content, status));
    }
}
