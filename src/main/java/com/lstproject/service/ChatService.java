package com.lstproject.service;

import com.lstproject.dto.ChatMessage;
import com.lstproject.dto.LoginRequest;
import com.lstproject.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatService {

    @Value("${socket.port:8889}")
    private int PORT;
    public void sendMsg(ChatMessage chatMessage) throws IOException {
        String message = chatMessage.getMessage();
        String sender = chatMessage.getSender();
        String roomId = chatMessage.getRoomId();
        ClientHandler clientHandler = null;
        if (!ChatServer.chatRooms.containsKey(roomId)) {
            clientHandler = addClient(sender, roomId);
        } else {
            Map<String, ClientHandler> chatMap = ChatServer.chatRooms.get(roomId);
            if (!chatMap.containsKey(sender)) {
                clientHandler = addClient(sender, roomId);
            }
        }
        clientHandler.sendMessage(message);
    }

    private ClientHandler addClient(String sender, String roomId) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(clientSocket);
        ChatServer.addClient(roomId, sender, clientHandler);
        return clientHandler;
    }

    // 在ChatService接口或实现类中添加以下方法
    public boolean joinRoom(String roomId, String roomPassword, String username) {
        // 验证聊天室密码（示例中假设密码验证通过）
        // 实际应用中应该有真实的密码验证逻辑
        
        // 检查聊天室中是否已存在同名用户
        Map<String, ClientHandler> clientMap = ChatServer.chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        
        // 检查用户名是否已存在
        if (clientMap.containsKey(username)) {
            return false; // 用户名已存在，加入失败
        }
        
        // 用户名未重复，允许加入
        // 注意：实际的客户端连接和加入操作在ClientHandler中处理
        // 这里只需要验证用户是否可以加入指定的聊天室
        return true; // 加入成功返回true，失败返回false
    }
    
    /**
     * 创建聊天室
     * @param roomId 聊天室ID
     * @param roomPassword 聊天室密码
     * @param username 创建者用户名
     * @return 是否创建成功
     */
    public boolean createRoom(String roomId, String roomPassword, String username) {
        // 调用ChatServer创建聊天室
        return ChatServer.createChatRoom(roomId, roomPassword);
    }
}