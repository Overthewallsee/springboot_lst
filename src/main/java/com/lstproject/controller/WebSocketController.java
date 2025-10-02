package com.lstproject.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ws")
@CrossOrigin(origins = "*")
public class WebSocketController {
    
    /**
     * 获取WebSocket连接信息
     * @return WebSocket连接信息
     */
    @GetMapping("/info")
    public Map<String, Object> getWebSocketInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("endpoint", "/ws/chat");
        response.put("protocol", "WebSocket");
        response.put("status", "available");
        response.put("message", "连接成功后可以通过WebSocket发送和接收消息");
        return response;
    }
}