package com.example.project.service.impl;

import com.example.project.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void revoke(String token, long ttlMillis) {
        if (token == null || token.isBlank() || ttlMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + token, "revoked", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + token));
    }
}
