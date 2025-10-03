package com.lstproject.controller;

import com.lstproject.dto.*;
import com.lstproject.service.AuthService;
import com.lstproject.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

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
    public ResponseEntity<CreateRoomResponse> joinChatRoom(@Valid @RequestBody ChatJoinRequest joinRequest) {
        try {
            // 调用ChatService处理加入逻辑
            boolean success = chatService.joinRoom(
                    joinRequest.getRoomId(),
                    joinRequest.getPassword(), // 使用 getPassword() 而不是 getRoomPassword()
                    joinRequest.getUsername()
            );
            if (success) {
                return ResponseEntity.ok(new CreateRoomResponse(chatService.queryUserList(joinRequest.getRoomId()), "成功加入聊天室"));
            } else {
                return ResponseEntity.badRequest().body( new CreateRoomResponse(null, "加入聊天室失败：用户已存在"));
            }
        } catch (Exception e) {
            logger.error("加入聊天室失败", e);
            return ResponseEntity.badRequest().body( new CreateRoomResponse(null, "加入聊天室失败：" + e.getMessage()));
        }
    }
    
    /**
     * 创建聊天室接口
     * @param createRoomRequest 包含聊天室ID、密码和用户名的请求对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<CreateRoomResponse> createChatRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest) {
        try {
            // 调用ChatService处理创建逻辑
            boolean success = chatService.createRoom(
                    createRoomRequest.getRoomId(),
                    createRoomRequest.getPassword(),
                    createRoomRequest.getUsername()
            );

            if (success) {
                boolean isSuccess = chatService.joinRoom(
                        createRoomRequest.getRoomId(),
                        createRoomRequest.getPassword(),
                        createRoomRequest.getUsername()
                );
                if (isSuccess) {
                    return ResponseEntity.ok(new CreateRoomResponse(chatService.queryUserList(createRoomRequest.getRoomId()), "创建聊天室成功"));
                } else {
                    return ResponseEntity.badRequest().body( new CreateRoomResponse(null, "创建聊天室失败：加入聊天室失败"));
                }
            } else {
                return ResponseEntity.badRequest().body( new CreateRoomResponse(null, "创建聊天室失败：聊天室已存在"));
            }
        } catch (Exception e) {
            logger.error("创建聊天室失败", e);
            return ResponseEntity.badRequest().body( new CreateRoomResponse(null, "创建聊天室失败：" + e.getMessage()));
        }
    }
    
    /**
     * 离开聊天室接口
     * @param leaveRoomRequest 包含聊天室ID和用户名的请求对象
     * @return 离开结果
     */
    @PostMapping("/leave")
    public ResponseEntity<String> leaveChatRoom(@Valid @RequestBody LeaveRoomRequest leaveRoomRequest) {
        try {
            // 调用ChatService处理离开逻辑
            boolean success = chatService.leaveRoom(
                    leaveRoomRequest.getRoomId(),
                    leaveRoomRequest.getUsername()
            );

            if (success) {
                return ResponseEntity.ok("成功离开聊天室");
            } else {
                return ResponseEntity.badRequest().body("离开聊天室失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("离开聊天室失败：" + e.getMessage());
        }
    }
}