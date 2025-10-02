package com.lstproject.controller;

import com.lstproject.dto.ChatJoinRequest;
import com.lstproject.dto.CreateRoomRequest;
import com.lstproject.dto.ChatMessage;
import com.lstproject.dto.LoginRequest;
import com.lstproject.dto.LoginResponse;
import com.lstproject.service.AuthService;
import com.lstproject.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity send(@Valid @RequestBody ChatMessage chatMessage, HttpServletRequest httpRequest) throws IOException {
        chatService.sendMsg(chatMessage);
        return ResponseEntity.ok("发送成功");
    }
    
    /**
     * 加入聊天室接口
     * @param joinRequest 包含聊天室ID、密码和用户名的请求对象
     * @return 加入结果
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinChatRoom(@Valid @RequestBody ChatJoinRequest joinRequest) {
        try {
            // 调用ChatService处理加入逻辑
            boolean success = chatService.joinRoom(
                    joinRequest.getRoomId(),
                    joinRequest.getRoomPassword(),
                    joinRequest.getUsername()
            );

            if (success) {
                return ResponseEntity.ok("成功加入聊天室");
            } else {
                return ResponseEntity.badRequest().body("加入聊天室失败：密码错误或用户已存在");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("加入聊天室失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建聊天室接口
     * @param createRoomRequest 包含聊天室ID、密码和用户名的请求对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<String> createChatRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        try {
            // 调用ChatService处理创建逻辑
            boolean success = chatService.createRoom(
                    createRoomRequest.getRoomId(),
                    createRoomRequest.getRoomPassword(),
                    createRoomRequest.getUsername()
            );

            if (success) {
                return ResponseEntity.ok("成功创建聊天室");
            } else {
                return ResponseEntity.badRequest().body("创建聊天室失败：聊天室已存在");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建聊天室失败：" + e.getMessage());
        }
    }
}