package com.lstproject.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

//@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
  
    @Override  
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        sessions.put(session.getId(), session);
        logger.info("WebSocket session established: " + session.getId());
    }  
  
    @Override  
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理接收到的消息  
        logger.info("Received message: " + message.getPayload());
    }  
  
    @Override  
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        logger.info("WebSocket session closed: " + session.getId());
    }  
  
    // 发送消息到指定客户端  
    public void sendMessageToUser(String sessionId, String message) throws IOException {
        WebSocketSession session = sessions.get(sessionId);  
        if (session != null && session.isOpen()) {  
            session.sendMessage(new TextMessage(message));  
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

        // 检查是否有路径参数
        if (parts.length > 2) {
            return parts[2]; // 返回路径参数作为房间ID
        }

        // 默认房间
        return "default";
    }
}