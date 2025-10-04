package com.lstproject.service;

import com.lstproject.dto.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

// 聊天室服务器主类
@Component
public class ChatServer {

    @Value("${socket.port:8889}")
    private int PORT;

    @Autowired
    private ChatRedisService chatRedisService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 存储聊天室客户端
    public static final ConcurrentHashMap<String, Map<String, ClientHandler>> chatRooms = new ConcurrentHashMap<>();

    public static ChatRedisService staticChatRedisService;

    // Kafka主题名称
    private static final String CHAT_TOPIC = "chat-messages";

    @PostConstruct
    public void init() {
        staticChatRedisService = this.chatRedisService;
    }

    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static KafkaTemplate<String, String> staticKafkaTemplate;

    @PostConstruct
    public void initStaticKafkaTemplate() {
        staticKafkaTemplate = this.kafkaTemplate;
    }

    // 通过Kafka发送消息
    public static void sendChatMessage(String message, String roomId) {
        // 将消息发送到Kafka主题
        String kafkaMessage = roomId + " |=| " + message;
        if (staticKafkaTemplate != null) {
            staticKafkaTemplate.send(CHAT_TOPIC, kafkaMessage);
        } else {
            System.err.println("KafkaTemplate is not initialized");
        }
    }

    // Kafka消息监听器
    @KafkaListener(topics = "chat-messages")
    public void listenChatMessages(String message) {
        // 解析消息
        String[] parts = message.split(" \\|=\\| ", 2);
        if (parts.length == 2) {
            String roomId = parts[0];
            String chatMessage = parts[1];

            // 广播消息给房间内的所有客户端
//            broadcastMessageToRoom(roomId, chatMessage);

            // 同时通过WebSocket广播消息
            ChatWebSocketHandler.broadcastMessage(roomId , chatMessage);
        }
    }

    // 广播消息给指定房间的所有客户端
    private static void broadcastMessageToRoom(String roomId, String message) {
        Map<String, ClientHandler> clientMap = chatRooms.get(roomId);
        if (clientMap != null) {
            clientMap.values().forEach(clientHandler -> {
                clientHandler.sendMessage(message);
            });
        }
    }

    // 创建聊天室（带密码）
    public static boolean createChatRoom(String roomId, String password) {
        // 检查Redis中聊天室是否已存在
        if (staticChatRedisService.isRoomExists(roomId)) {
            return false; // 聊天室已存在
        }
        // 保存聊天室信息到Redis
        staticChatRedisService.saveChatRoom(new ChatRoom(roomId, password));
        return true;
    }

    // 验证聊天室密码
    public static boolean verifyRoomPassword(String roomId, String password) {
        // 从Redis验证聊天室密码
        return staticChatRedisService.verifyRoomPassword(roomId, password);
    }

    // 检查聊天室是否存在
    public static boolean isRoomExists(String roomId) {
        // 检查Redis中聊天室是否存在
        return staticChatRedisService.isRoomExists(roomId);
    }

    // 获取客户端处理程序
    public static ClientHandler getClientHandler(String roomId, String name) {
        Map<String, ClientHandler> clientMap = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        ClientHandler clientHandler = clientMap.get(name);

        if (clientHandler == null) {
            clientHandler = new ClientHandler(null); // 创建一个空的客户端处理器
            clientMap.put(name, clientHandler);
            chatRooms.put(roomId, clientMap);
        }

        return clientHandler;
    }

    // 移除客户端
    public static void removeClient(String roomId, String name) {
        Map<String, ClientHandler> clientMap = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        clientMap.remove(name);
        // 从Redis中的聊天室用户列表中移除用户
        staticChatRedisService.removeUserFromRoom(roomId, name);
        broadcastMessage(roomId + " := " + name + " 离开了聊天室", name);
    }

    // 获取在线用户列表
    public static String getOnlineUsers(String roomId) {
        // 从Redis获取聊天室用户列表
        Set<String> users = staticChatRedisService.getRoomUsers(roomId);
        if (users != null && !users.isEmpty()) {
            return "在线用户: " + String.join(", ", users);
        } else {
            return "在线用户: 暂无";
        }
    }

    // 广播消息
    public static void broadcastMessage(String message) {
        // 通过Kafka发送消息
        sendChatMessage(message, "default");
    }

    // 广播消息（排除指定用户）
    public static void broadcastMessage(String message, String excludeUser) {
        // 通过Kafka发送消息
        sendChatMessage(message, "default");
    }
}