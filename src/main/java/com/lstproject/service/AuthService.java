package com.lstproject.service;

import com.lstproject.dto.LoginRequest;
import com.lstproject.dto.LoginResponse;
import com.lstproject.entity.User;
import com.lstproject.exception.RateLimitExceededException;
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
        rateLimitService.isRateLimited(clientIp);
        
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            rateLimitService.incrementAttempts(clientIp);
            throw new RuntimeException("Invalid username or password");
        }
        
        // 重置限流计数
//        rateLimitService.resetAttempts(clientIp);



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

    public LoginResponse register(LoginRequest request, String clientIp) {
        // 检查是否被限流
        rateLimitService.isRateLimited(clientIp);

        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            rateLimitService.incrementAttempts(clientIp);
            throw new RuntimeException("Username already exists");
        }

        // 检查手机号是否已存在
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            rateLimitService.incrementAttempts(clientIp);
            throw new RuntimeException("Phone number already registered");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());

        userRepository.save(user);

        // 重置限流计数
        rateLimitService.resetAttempts(clientIp);

        // 生成token
        String token = UUID.randomUUID().toString();

        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("Registration successful");
        response.setToken(token);

        return response;
    }

}
