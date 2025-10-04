package com.lstproject.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChatWebSocketHandler implements WebSocketHandler {
    
    // 存储所有连接的客户端
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
//    private final ConcurrentHashMap<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    // 存储用户与会话的映射关系
    private static final ConcurrentHashMap<String, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();


    // JSON处理工具
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 添加新连接到会话列表
        sessions.add(session);
        System.out.println("新的WebSocket连接建立: " + session.getId());
        String roomId = getRoomId(session);
        userSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        // 发送欢迎消息
        ObjectNode welcomeMessage = objectMapper.createObjectNode();
        welcomeMessage.put("type", "system");
        welcomeMessage.put("message", "欢迎连接到聊天服务器!");
        welcomeMessage.put("sessionId", session.getId());
        session.sendMessage(new TextMessage(welcomeMessage.toString()));
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        System.out.println("收到消息: " + payload);
        
        try {
            // 尝试解析JSON消息
            ObjectNode jsonMessage = (ObjectNode) objectMapper.readTree(payload);
            String type = jsonMessage.get("type").asText();
            
            switch (type) {
                case "join":
                    handleJoinMessage(session, jsonMessage);
                    break;
                case "chat":
                    handleChatMessage(session, jsonMessage);
                    break;
                case "leave":
                    handleLeaveMessage(session, jsonMessage);
                    break;
                default:
                    // 普通文本消息，直接广播
                    broadcastMessage(payload);
            }
        } catch (Exception e) {
            // 非JSON消息，直接广播
            broadcastMessage(payload);
        }
    }
    
    /**
     * 处理用户加入聊天室的消息
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void handleJoinMessage(WebSocketSession session, ObjectNode message) throws IOException {
        String username = message.get("username").asText();
        String roomId = message.get("roomId").asText();
        
        // 将用户与会话关联
//        userSessions.put(username, session);
        
        // 发送确认消息
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "system");
        response.put("message", username + " 已加入聊天室 " + roomId);
        session.sendMessage(new TextMessage(response.toString()));
        
        // 通知其他用户
        ObjectNode broadcast = objectMapper.createObjectNode();
        broadcast.put("type", "user_joined");
        broadcast.put("username", username);
        broadcast.put("roomId", roomId);
        broadcastMessageToOthers(session, broadcast.toString());
    }
    
    /**
     * 处理聊天消息
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void handleChatMessage(WebSocketSession session, ObjectNode message) throws IOException {
        String username = message.has("username") ? message.get("username").asText() : "未知用户";
        String roomId = message.has("roomId") ? message.get("roomId").asText() : "默认房间";
        String content = message.get("content").asText();
        
        // 广播聊天消息
        ObjectNode broadcast = objectMapper.createObjectNode();
        broadcast.put("type", "chat");
        broadcast.put("username", username);
        broadcast.put("roomId", roomId);
        broadcast.put("content", content);
        broadcast.put("timestamp", System.currentTimeMillis());
        broadcastMessage(broadcast.toString());
    }
    
    /**
     * 处理用户离开聊天室的消息
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void handleLeaveMessage(WebSocketSession session, ObjectNode message) throws IOException {
        String username = message.get("username").asText();
        String roomId = message.get("roomId").asText();
        
//        // 移除用户会话关联
//        userSessions.remove(username);
        
        // 通知其他用户
        ObjectNode broadcast = objectMapper.createObjectNode();
        broadcast.put("type", "user_left");
        broadcast.put("username", username);
        broadcast.put("roomId", roomId);
        broadcastMessageToOthers(session, broadcast.toString());
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + exception.getMessage());
        sessions.remove(session);
        removeUserSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // 移除关闭的连接
        sessions.remove(session);
        removeUserSession(session);
        System.out.println("WebSocket连接关闭: " + session.getId() + ", 状态: " + closeStatus);
    }
    
    /**
     * 从用户会话映射中移除会话
     * @param session WebSocket会话
     */
    private void removeUserSession(WebSocketSession session) {
        String roomId = getRoomId(session);
        userSessions.get(roomId).remove(session.getId());
        if (userSessions.get(roomId).isEmpty()) {
            userSessions.remove(roomId);
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 广播消息给所有连接的客户端
     * @param message 要发送的消息
     */
    public static void broadcastMessage(String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("发送消息失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 广播消息给除了指定会话外的所有客户端
     * @param excludeSession 要排除的会话
     * @param message 要发送的消息
     */
    public static void broadcastMessageToOthers(WebSocketSession excludeSession, String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen() && !session.getId().equals(excludeSession.getId())) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("发送消息失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 给特定用户发送消息
     * @param username 用户名
     * @param message 要发送的消息
     */
    public static void sendMessageToUser(String roomId, String username, String message) {
        Map<String, WebSocketSession> roomMap = userSessions.get(roomId);
        if (roomMap != null) {
            WebSocketSession session = roomMap.get(username);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("发送消息失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 从WebSocket会话中提取房间ID
     * @param session WebSocket会话
     * @return 房间ID
     */
    private String getRoomId(WebSocketSession session) {
        String uri = session.getUri().getPath();
        String[] parts = uri.split("/");
        Arrays.sort(parts, Comparator.reverseOrder()); ;
        // 默认房间
        return parts[0];
    }
}