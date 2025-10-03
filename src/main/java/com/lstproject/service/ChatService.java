package com.lstproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lstproject.dto.ChatMessage;
import com.lstproject.dto.UserDTO;
import com.lstproject.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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
        // 普通聊天消息，通过Kafka发送
        String msg = sender + ": " + message;
        ChatServer.sendChatMessage(msg, roomId);
//        clientHandler.sendMessage(message);
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
        // 验证聊天室是否存在
        if (!ChatServer.isRoomExists(roomId)) {
            throw new RuntimeException("聊天室不存在");
        }
        
        // 验证聊天室密码
        if (!ChatServer.verifyRoomPassword(roomId, roomPassword)) {
            throw new RuntimeException("聊天室密码错误");
        }
        
        // 检查聊天室中是否已存在同名用户
//        Map<String, ClientHandler> clientMap = ChatServer.chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());

        // 检查用户名是否已存在
        if (ChatServer.staticChatRedisService.isUserInRoom(roomId, username)) {
            return false; // 用户名已存在，加入失败
        }
        
        // 用户名未重复，允许加入
        // 将用户添加到聊天室的用户列表中
        ChatServer.staticChatRedisService.addUserToRoom(roomId, username);
        
        // 通过WebSocket通知所有客户端有新用户加入
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "user_joined");
            message.put("username", username);
            message.put("roomId", roomId);
            message.put("message", username + " 加入了聊天室");
            ChatWebSocketHandler.broadcastMessage(message.toString());
        } catch (Exception e) {
            System.err.println("发送WebSocket消息失败: " + e.getMessage());
        }
        
        // 加入成功返回true
        return true;
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
    
    /**
     * 用户离开聊天室
     * @param roomId 聊天室ID
     * @param username 用户名
     * @return 是否成功离开
     */
    public boolean leaveRoom(String roomId, String username) {
        // 从聊天室中移除用户
        ChatServer.removeClient(roomId, username);
        
        // 检查聊天室是否为空
        Set<String> users = ChatServer.staticChatRedisService.getRoomUsers(roomId);
        if (users == null || users.isEmpty()) {
            // 如果聊天室为空，删除聊天室
            ChatServer.staticChatRedisService.deleteChatRoom(roomId);
            ChatServer.chatRooms.remove(roomId);
        }
        
        return true;
    }

    public List<UserInfoDTO> queryUserList(String roomId) {
        Set<String> roomUsers = ChatServer.staticChatRedisService.getRoomUsers(roomId);
//        Map<String, ClientHandler> userMap = ChatServer.chatRooms.get(roomId);
        List<UserInfoDTO> userInfoList = new ArrayList<>();
        if (roomUsers == null || roomUsers.isEmpty()) {
            return userInfoList;
        }
        roomUsers.forEach(username -> {
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setName(username);
            userInfoDTO.setColor(getRandomColor());
            userInfoDTO.setId(UUID.randomUUID().toString());
            userInfoList.add(userInfoDTO);
        });
        return userInfoList;
    }

    private String getRandomColor() {
        String[] colors = {
                "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
                "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F",
                "#BB8FCE", "#85C1E9", "#F8C471", "#82E0AA"
        };
        double floor = Math.floor(Math.random() * colors.length);
        return colors[(int) floor];
    }
  
}