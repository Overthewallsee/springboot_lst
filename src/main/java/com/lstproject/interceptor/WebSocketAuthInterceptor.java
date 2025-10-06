package com.lstproject.interceptor;

import com.lstproject.service.UserService;
import com.lstproject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil tokenProvider; // 你的JWT工具类

    private final UserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // 从请求参数中获取token
        URI uri = request.getURI();
        String token = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
        String username = tokenProvider.extractUsername(token);
        // Validate Token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != username && authentication != null) {
            return username.equals(authentication.getName());
        }
//        if (token != null && tokenProvider.validateToken(token)) {
//            // 解析token获取用户信息
//            Authentication auth = tokenProvider.getAuthentication(token);
//            // 将认证信息存入上下文
//            SecurityContextHolder.getContext().setAuthentication(auth);
//            // 将用户信息存入属性，供后续使用
//            attributes.put("user", auth.getPrincipal());
//            return true;
//        }
        
        // 认证失败，拒绝握手
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 清除上下文，避免线程安全问题
        SecurityContextHolder.clearContext();
    }
}