package com.lstproject.config;

import org.springframework.stereotype.Component;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

//@ServerEndpoint("/lst/ws/chat/{roomId}")
//@Component
public class ChatEndpoint {
    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") String roomId) {
        // 从查询参数中获取token
        String queryString = session.getQueryString();
        String token = null;
        if (queryString != null) {
            Map<String, String> queryParams = parseQueryParams(queryString);
            token = queryParams.get("token");
        }
        // 连接建立处理
        System.out.println("WebSocket连接已建立，房间号: " + roomId + ", Token: " + token);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("-----------接收到消息-----------");
        // 消息处理逻辑
    }
    
    private Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        if (queryString != null && !queryString.isEmpty()) {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }
}