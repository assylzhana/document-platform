package com.assylzhana.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveToken(String token, String username) {
        redisTemplate.opsForValue().set(token, username, Duration.ofMinutes(30));
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }

    public void blacklistToken(String token) {
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofDays(365));
    }

    public void removeToken(String token) {
        redisTemplate.delete(token);
    }
}
