package com.lstproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lstproject.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    

    private final ChatRedisService chatRedisService;
    private final RedisTemplate<String, String> redisTemplate;
//    private final KafkaTemplate<String, String> kafkaTemplate;
    
    /**
     * 加入聊天室
     * @param roomId 聊天室ID
     * @param roomPassword 聊天室密码
     * @param username 用户名
     * @return 是否加入成功
     */
    public boolean joinRoom(String roomId, String roomPassword, String username) {
        // 验证聊天室密码
        if (!ChatServer.verifyRoomPassword(roomId, roomPassword)) {
            return false; // 密码错误，加入失败
        }
        
        Map<String, ClientHandler> clientMap = ChatServer.chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        
        // 检查用户名是否已存在
        if (clientMap.containsKey(username)) {
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
        return ChatServer.createChatRoom(roomId, roomPassword);
    }
    
    /**
     * 离开聊天室
     * @param roomId 聊天室ID
     * @param username 用户名
     * @return 是否离开成功
     */
    public boolean leaveRoom(String roomId, String username) {
        // 从聊天室中移除用户
        ChatServer.removeClient(roomId, username);
        
        // 从Redis中的聊天室用户列表中移除用户
        chatRedisService.removeUserFromRoom(roomId, username);
        
        // 通知其他用户该用户已离开
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "user_left");
            message.put("username", username);
            message.put("roomId", roomId);
            message.put("message", username + " 离开了聊天室");
            ChatWebSocketHandler.broadcastMessage(message.toString());
        } catch (Exception e) {
            System.err.println("发送WebSocket消息失败: " + e.getMessage());
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
    
    public void sendMsg(com.lstproject.dto.ChatMessage chatMessage) {
        // 通过Kafka发送消息
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", "chat");
            message.put("username", chatMessage.getSender());
            message.put("roomId", chatMessage.getRoomId());
            message.put("content", chatMessage.getMessage());
            message.put("timestamp", System.currentTimeMillis());
            ChatWebSocketHandler.broadcastMessage(message.toString());
        } catch (Exception e) {
            System.err.println("发送WebSocket消息失败: " + e.getMessage());
        }
    }
}