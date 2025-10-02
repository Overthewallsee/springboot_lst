package com.lstproject;

import com.lstproject.service.ChatServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class LstProjectApplication {
    
    @Autowired
    private ChatServer chatServer;
    
    @PostConstruct
    public void startChatServer() {
        // 启动聊天室服务器
//        chatServer.startChatServer();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(LstProjectApplication.class, args);
    }
}