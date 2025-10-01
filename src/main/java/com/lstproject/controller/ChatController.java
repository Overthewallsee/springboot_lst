package com.lstproject.controller;

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
}
