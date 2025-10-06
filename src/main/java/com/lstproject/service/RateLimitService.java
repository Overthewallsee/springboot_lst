package com.lstproject.service;

import com.lstproject.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${rate-limit.max-attempts:1}")
    private int maxAttempts;
    
    @Value("${rate-limit.window-seconds:5}")
    private int windowSeconds;
    
    public void isRateLimited(String key) {
        // 检查是否超过最大尝试次数
        int attempts = Integer.parseInt(incrementAttempts(key));
        if (attempts > maxAttempts) {
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }
    }
    
    public String incrementAttempts(String key) {
        String redisKey = "rate_limit:" + key;
        String attempts = redisTemplate.opsForValue().get(redisKey);
        if (attempts == null) {
            redisTemplate.opsForValue().set(redisKey, "1", windowSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().increment(redisKey, 1);
        }
        return redisTemplate.opsForValue().get(redisKey);
    }
    
    public void resetAttempts(String key) {
        String redisKey = "rate_limit:" + key;
        redisTemplate.delete(redisKey);
    }
}
