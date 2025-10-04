package com.lstproject.config;

import com.lstproject.interceptor.ChatNameWebSocketHandler;
import com.lstproject.interceptor.MyWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.lstproject.service.ChatWebSocketHandler;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

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

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatNameWebSocketHandler(), "/lst/ws/chat/{roomId}").setAllowedOrigins("*");
//        registry.addHandler(new ChatWebSocketHandler(), "/ws-with-sockjs").setAllowedOrigins("*").withSockJS();
    }
}