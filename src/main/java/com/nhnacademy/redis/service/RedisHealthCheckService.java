package com.nhnacademy.redis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisHealthCheckService {

    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void checkRedisConnection() {
        try {
            redisTemplate.opsForValue().set("ping", "pong");
            log.info("Redis 연결 성공!");
        } catch (Exception e) {
            log.error("Redis 연결 실패!", e);
        }
    }
}