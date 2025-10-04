package com.lstproject.service;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.lstproject.dto.UserDTO;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

public class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    
    // 存储所有连接的客户端
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

//    private final ConcurrentHashMap<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    // 房间号存储用户与会话的映射关系
    private static final ConcurrentHashMap<String, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, String> userSessionsRoom = new ConcurrentHashMap<>();


    // JSON处理工具
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 添加新连接到会话列表
        sessions.add(session);
        System.out.println("新的WebSocket连接建立: " + session.getId());
        String name = session.getPrincipal().getName();
        String roomId = getRoomId(session);
        userSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        userSessionsRoom.put(session.getId(), roomId);
        // 发送欢迎消息
        ObjectNode welcomeMessage = objectMapper.createObjectNode();
        welcomeMessage.put("type", "user_list");
        welcomeMessage.put("message", "欢迎"+name+"连接到聊天服务器!");
        welcomeMessage.put("name", "系统");
        welcomeMessage.put("username", "系统");
        Map<String, WebSocketSession> userMap = userSessions.get(roomId);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (WebSocketSession value : userMap.values()) {
            UserDTO userDTO = new UserDTO();
            userDTO.setName(value.getPrincipal().getName());
            arrayNode.add(objectMapper.valueToTree(userDTO));
        }
        welcomeMessage.set("users", arrayNode);
        session.sendMessage(new TextMessage(welcomeMessage.toString()));
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        System.out.println("收到消息: " + payload);
        
        try {
            // 尝试解析JSON消息
            JSONObject jsonObject = JSONObject.parseObject(payload);
            String type = jsonObject.getString("type");
            String roomId = userSessionsRoom.get(session.getId());
            switch (type) {
                case "join":
                    handleJoinMessage(session, jsonObject);
                    break;
                case "chat":
                    handleChatMessage(session, jsonObject);
                    break;
                case "leave":
                    handleLeaveMessage(session, jsonObject);
                    break;
                default:
                    // 普通文本消息，直接广播
                    broadcastMessage(roomId, payload);
            }
        } catch (Exception e) {
            // 非JSON消息，直接广播
            logger.error("无法解析JSON消息: " + e.getMessage());
//            broadcastMessage(roomId, payload);
        }
    }
    
    /**
     * 处理用户加入聊天室的消息
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void handleJoinMessage(WebSocketSession session, JSONObject message) throws IOException {
        String username = session.getPrincipal().getName();
        String roomId = getRoomId(session);
        
        // 将用户与会话关联
//        userSessions.put(username, session);
        
        // 发送确认消息
        ObjectNode response = objectMapper.createObjectNode();
        response.put("type", "system");
        response.put("message", username + " 已加入聊天室 " + roomId);
        session.sendMessage(new TextMessage(response.toString()));
        
        // 通知其他用户
        ObjectNode broadcast = objectMapper.createObjectNode();
        broadcast.put("type", "join");
        broadcast.put("username", username);
        broadcast.put("roomId", roomId);
        ObjectNode welcomeMessage1 = objectMapper.createObjectNode();
        welcomeMessage1.put("name", username);
        broadcast.put("user", welcomeMessage1);
        broadcastMessage(roomId, broadcast.toString());
    }
    
    /**
     * 处理聊天消息
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void handleChatMessage(WebSocketSession session, JSONObject message) throws IOException {
        if (session == null || session.getPrincipal() == null || session.getPrincipal().getName() == null) {
            throw  new RemoteException("用户未登录");
        }
        String username = session.getPrincipal().getName();
        String roomId = getRoomId(session);
        String content = message.getJSONObject("message").getString("content");
        // 广播聊天消息
        ObjectNode broadcast = objectMapper.createObjectNode();
        ObjectNode messageNode = objectMapper.createObjectNode();
        broadcast.put("type", "chat");
        messageNode.put("username", username);
        messageNode.put("roomId", roomId);
        messageNode.put("content", content);
        messageNode.put("timestamp", System.currentTimeMillis());
        broadcast.put("message", messageNode);
        broadcastMessage(roomId, broadcast.toString());
    }
    
    /**
     * 处理用户离开聊天室的消息
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void handleLeaveMessage(WebSocketSession session, JSONObject message) throws IOException {
        String username = message.getString("username");
        String roomId = message.getString("roomId");
        
//        // 移除用户会话关联
//        userSessions.remove(username);

        
        // 通知其他用户
        ObjectNode broadcast = objectMapper.createObjectNode();
        broadcast.put("type", "leave");
        broadcast.put("username", username);
        broadcast.put("roomId", roomId);
        broadcastMessageToOthers(session, broadcast.toString());
        removeUserSession(session);
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
        userSessionsRoom.remove(session.getId());
        ChatServer.staticChatRedisService.removeUserFromRoom(roomId, session.getPrincipal().getName());
        Set<String> roomUsers = ChatServer.staticChatRedisService.getRoomUsers(roomId);
        if (roomUsers == null || roomUsers.isEmpty()) {
            ChatServer.staticChatRedisService.deleteChatRoom(roomId);
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
    public static void broadcastMessage(String roomId, String message) {
        if (userSessions.containsKey(roomId)) {
            Map<String, WebSocketSession> socketSessionMap = userSessions.get(roomId);
            Collection<WebSocketSession> values = socketSessionMap.values();
            for (WebSocketSession session : values) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        System.err.println("发送消息失败: " + e.getMessage());
                    }
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
        String id = excludeSession.getId();
        String roomId = userSessionsRoom.get(id);
        Map<String, WebSocketSession> webSocketSessionMap = userSessions.get(roomId);
        Collection<WebSocketSession> values = webSocketSessionMap.values();
        for (WebSocketSession session : values) {
            if (session.isOpen() && !session.getId().equals(id)) {
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
        // 默认房间
        return parts[parts.length - 1];
    }
}