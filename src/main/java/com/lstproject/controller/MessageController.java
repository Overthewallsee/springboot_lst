package com.lstproject.controller;

import com.lstproject.interceptor.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/message")
public class MessageController {  
  
//    @Autowired
    private MyWebSocketHandler webSocketHandler;
  
    @PostMapping("/push")
    public ResponseEntity<?> pushMessage(@RequestParam String sessionId, @RequestParam String message) {
        try {  
            webSocketHandler.sendMessageToUser(sessionId, message);  
            return ResponseEntity.ok("Message sent successfully");  
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send message");
        }  
    }  
}