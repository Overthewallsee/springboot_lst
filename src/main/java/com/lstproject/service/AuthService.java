package com.lstproject.service;

import com.lstproject.dto.LoginRequest;
import com.lstproject.dto.LoginResponse;
import com.lstproject.entity.User;
import com.lstproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;
    
    public LoginResponse login(LoginRequest request, String clientIp) {
        // 检查是否被限流
        if (rateLimitService.isRateLimited(clientIp)) {
            throw new RuntimeException("Too many login attempts. Please try again later.");
        }
        
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            rateLimitService.incrementAttempts(clientIp);
            throw new RuntimeException("Invalid username or password");
        }
        
        // 重置限流计数
        rateLimitService.resetAttempts(clientIp);
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成token (简化实现)
        String token = UUID.randomUUID().toString();
        
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setToken(token);
        
        return response;
    }
}
