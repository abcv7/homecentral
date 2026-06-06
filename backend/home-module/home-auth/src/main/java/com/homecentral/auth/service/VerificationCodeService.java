package com.homecentral.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class VerificationCodeService {

    private static final String KEY_PREFIX = "auth:code:";
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    public VerificationCodeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndStore(String purpose, String email) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        String key = buildKey(purpose, email);
        redisTemplate.opsForValue().set(key, code, TTL);
        return code;
    }

    public boolean verify(String purpose, String email, String code) {
        if (code == null || code.isBlank()) return false;
        String key = buildKey(purpose, email);
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) return false;
        if (!stored.equals(code.trim())) return false;
        redisTemplate.delete(key);
        return true;
    }

    public void clear(String purpose, String email) {
        redisTemplate.delete(buildKey(purpose, email));
    }

    private String buildKey(String purpose, String email) {
        return KEY_PREFIX + purpose + ":" + email.toLowerCase();
    }
}
