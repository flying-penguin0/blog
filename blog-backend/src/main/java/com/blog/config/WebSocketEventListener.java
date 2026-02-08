package com.blog.config;

import com.blog.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 * 用于处理用户连接和断开连接事件
 */
@Component
public class WebSocketEventListener {
    
    @Autowired
    private ChatRoomService chatRoomService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        System.out.println("WebSocket连接建立: " + sessionId);
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 从session中获取userId
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        
        if (userId != null) {
            System.out.println("用户断开连接: " + userId);
            
            // 用户下线
            chatRoomService.userOffline(userId);
            
            // 广播用户下线消息
            messagingTemplate.convertAndSend("/topic/chatroom/status", "offline:" + userId);
        }
    }
}
