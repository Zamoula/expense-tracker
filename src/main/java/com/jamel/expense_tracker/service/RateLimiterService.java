package com.jamel.expense_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Value("${rate-limit.requests:2}")
    private int maxRequests;
    
    @Value("${rate-limit.seconds:60}")
    private int timeWindow;
    
    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public boolean isAllowed(String key) {
        String redisKey = "rate:" + key;
        
        // Get current count
        String countStr = redisTemplate.opsForValue().get(redisKey);
        long count = countStr == null ? 0 : Long.parseLong(countStr);
        
        if (count >= maxRequests) {
            return false;  // Rate limit exceeded
        }
        
        // Increment count
        redisTemplate.opsForValue().increment(redisKey);
        
        // Set expiry on first request
        if (count == 0) {
            redisTemplate.expire(redisKey, timeWindow, TimeUnit.SECONDS);
        }
        
        return true;  // Request allowed
    }
}
