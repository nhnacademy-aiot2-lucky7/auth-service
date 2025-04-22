package com.nhnacademy.redis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * RedisHealthCheckService 클래스는 Redis 서버의 연결 상태를 점검하는 서비스 클래스입니다.
 * <p>
 * 이 클래스는 Redis 연결이 정상적인지 확인하기 위해 RedisTemplate을 사용하여 Redis에 간단한 테스트 데이터를 저장합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisHealthCheckService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 서버 연결 상태를 확인하는 메서드입니다.
     * <p>
     * Redis 서버에 "ping"이라는 키로 "pong" 값을 저장하여 Redis 서버와의 연결을 점검합니다.
     * 연결이 정상적이면 "Redis 연결 성공!" 메시지를 로그로 출력하고,
     * 예외가 발생하면 "Redis 연결 실패!" 메시지를 로그로 출력합니다.
     * </p>
     */
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