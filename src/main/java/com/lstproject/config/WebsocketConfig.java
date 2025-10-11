package com.lstproject.config;

import com.lstproject.interceptor.WebSocketAuthInterceptor;
import com.lstproject.service.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

//    @Bean
//    public ServerEndpointExporter serverEndpointExporter() {
//        return new ServerEndpointExporter();
//    }
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(new MyWebSocketHandler(), "lst/ws/chat/{roomId}")
//                .setAllowedOrigins("*");
//    }

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat/{roomId}")
                .addInterceptors(webSocketAuthInterceptor);
//                .setAllowedOrigins("*");
//                .setAllowedOriginPatterns("*") // 根据需求配置允许的源
//                .withSockJS(); // 如果需要兼容不支持WebSocket的浏览器
//        registry.addHandler(new ChatWebSocketHandler(), "/ws-with-sockjs").setAllowedOrigins("*").withSockJS();
    }
}