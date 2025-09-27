package com.lstproject.service;

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
    
    @Value("${rate-limit.max-attempts:5}")
    private int maxAttempts;
    
    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;
    
    public boolean isRateLimited(String key) {
        String redisKey = "rate_limit:" + key;
        String attempts = redisTemplate.opsForValue().get(redisKey);
        
        if (attempts == null) {
            return false;
        }
        
        return Integer.parseInt(attempts) >= maxAttempts;
    }
    
    public void incrementAttempts(String key) {
        String redisKey = "rate_limit:" + key;
        String attempts = redisTemplate.opsForValue().get(redisKey);
        
        if (attempts == null) {
            redisTemplate.opsForValue().set(redisKey, "1", windowSeconds, TimeUnit.SECONDS);
        } else {
            int currentAttempts = Integer.parseInt(attempts);
            redisTemplate.opsForValue().increment(redisKey, 1);
        }
    }
    
    public void resetAttempts(String key) {
        String redisKey = "rate_limit:" + key;
        redisTemplate.delete(redisKey);
    }
}
