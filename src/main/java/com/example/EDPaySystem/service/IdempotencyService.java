package com.example.EDPaySystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    private static final String KEY_PREFIX = "notification:processed:";
    private static final long TTL_HOURS = 24;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean isAlreadyProcessed(String paymentId) {
        String key = KEY_PREFIX + paymentId;
        return redisTemplate.hasKey(key);
    }

    public void markAsProcessed(String paymentId) {
        String key = KEY_PREFIX + paymentId;
        redisTemplate.opsForValue().set(key, "processed", TTL_HOURS, TimeUnit.HOURS);
    }
}
