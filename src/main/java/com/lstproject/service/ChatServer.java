package com.lstproject.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

// 聊天室服务器主类
@Component
public class ChatServer implements InitializingBean {

    @Value("${socket.port:8889}")
    private int PORT;
    public static final ConcurrentHashMap<String, Map<String, ClientHandler>> chatRooms = new ConcurrentHashMap<>();

    private static ExecutorService threadPool = Executors.newCachedThreadPool();



    // 广播消息给所有客户端
    public static void broadcastMessage(String message, String senderName) {
        String[] split = message.split(" := ", 3);
        Map<String, ClientHandler> clientMap = chatRooms.get(split[0]);
        clientMap.keySet().forEach(entry -> {
            ClientHandler clientHandlers = clientMap.get(entry);
            clientHandlers.sendMessage(split[1] + ": " + split[2]);
        });
    }

    // 注册客户端
    public static ClientHandler addClient(String roomId, String name, ClientHandler clientHandler) {
        Map<String, ClientHandler> clientMap = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        clientMap.put(name, clientHandler);
        chatRooms.put(roomId, clientMap);
        broadcastMessage(name + " 加入了聊天室", name);
        return  clientHandler;
    }

    // 移除客户端
    public static void removeClient(String roomId, String name) {
        Map<String, ClientHandler> clientMap = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        clientMap.remove(name);
        broadcastMessage(name + " 离开了聊天室", name);
    }

    // 获取在线用户列表
    public static String getOnlineUsers(String roomId) {
        Map<String, ClientHandler> clientMap = chatRooms.getOrDefault(roomId, new ConcurrentHashMap<>());
        return "在线用户: " + String.join(", ", clientMap.keySet());
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
