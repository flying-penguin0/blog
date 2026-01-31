package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Message;
import com.blog.service.MessageService;
import com.blog.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 留言控制器
 */
@Tag(name = "留言管理")
@RestController
@RequestMapping("/message")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 创建留言
     */
    @Operation(summary = "创建留言")
    @PostMapping
    public Result<String> createMessage(@RequestBody Message message, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        message.setUserId(userId);
        messageService.createMessage(message);
        
        // 返回留言状态
        return Result.success(message.getStatus());
    }
    
    /**
     * 获取留言列表
     */
    @Operation(summary = "获取留言列表")
    @GetMapping
    public Result<PageResult<Message>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status
    ) {
        PageResult<Message> result = messageService.getMessageList(page, size, status);
        return Result.success(result);
    }
    
    /**
     * 获取我的留言列表
     */
    @Operation(summary = "获取我的留言列表")
    @GetMapping("/my")
    public Result<PageResult<Message>> getMyMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String status,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        PageResult<Message> result = messageService.getMyMessages(userId, page, size, content, status);
        return Result.success(result);
    }
    
    /**
     * 删除留言
     */
    @Operation(summary = "删除留言")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return Result.success();
    }
    
    /**
     * 审核留言
     */
    @Operation(summary = "审核留言")
    @PutMapping("/{id}/audit")
    public Result<Void> auditMessage(@PathVariable Long id, @RequestParam String status) {
        messageService.auditMessage(id, status);
        return Result.success();
    }
}
