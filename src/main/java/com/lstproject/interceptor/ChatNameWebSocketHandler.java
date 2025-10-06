package com.lstproject.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lstproject.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatNameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatNameWebSocketHandler.class);


    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("WebSocket connection established: " + session.getId());
        
        // 发送连接成功消息
        ChatMessage welcomeMsg = new ChatMessage();
        welcomeMsg.setMessage("Connected to WebSocket successfully");
        welcomeMsg.setType(ChatMessage.MessageType.CHAT);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMsg)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("Received message: " + payload);

        try {
            // 解析消息
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

            // 广播消息给所有连接的客户端
            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen()) {
                    chatMessage.setUserName(chatMessage.getSender());
                    chatMessage.setTimestamp(System.currentTimeMillis());
                    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                }
            }
        } catch (Exception e) {
            // 如果不是ChatMessage格式，直接广播原始消息
            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(message);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: " + session.getId() + ", status: " + status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        System.err.println("Exception details: " + exception);
        exception.printStackTrace();
        sessions.remove(session);
        // 尝试关闭会话
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception e) {
            // 忽略关闭异常
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}