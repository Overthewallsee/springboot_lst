package com.lstproject.service;

import com.lstproject.dto.ChatRoom;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

// 聊天室服务器主类
@Component
public class ChatServer implements InitializingBean {

    @Value("${socket.port:8889}")
    private int PORT;
    
    @Autowired
    private ChatRedisService chatRedisService;
    
    // 存储聊天室客户端
    public static final ConcurrentHashMap<String, Map<String, ClientHandler>> chatRooms = new ConcurrentHashMap<>();

    public static ChatRedisService staticChatRedisService;
    
    @PostConstruct
    public void init() {
        staticChatRedisService = this.chatRedisService;
    }

    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    // 广播消息给所有客户端
    public static void broadcastMessage(String message, String senderName) {
        String[] split = message.split(" := ", 3);
        Map<String, ClientHandler> clientMap = chatRooms.get(split[0]);
        if (clientMap != null) {
            clientMap.keySet().forEach(entry -> {
                ClientHandler clientHandlers = clientMap.get(entry);
                clientHandlers.sendMessage(split[1] + ": " + split[2]);
            });
        }
    }

    // 注册客户端
    public static ClientHandler addClient(String roomId, String name, ClientHandler clientHandler) {
        Map<String, ClientHandler> clientMap = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        clientMap.put(name, clientHandler);
        chatRooms.put(roomId, clientMap);
        // 将用户添加到Redis中的聊天室用户列表
        staticChatRedisService.addUserToRoom(roomId, name);
        broadcastMessage(roomId + " := " + name + " 加入了聊天室", name);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("聊天室服务器启动，监听端口: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}