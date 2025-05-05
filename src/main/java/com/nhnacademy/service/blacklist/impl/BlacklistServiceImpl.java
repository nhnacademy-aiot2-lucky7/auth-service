package com.nhnacademy.service.blacklist.impl;

import com.nhnacademy.service.blacklist.BlacklistService;
import com.nhnacademy.token.provider.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * AccessToken 블랙리스트 등록을 처리하는 서비스 구현체입니다.
 * <p>
 * 로그아웃 시 access token을 Redis에 저장하여 재사용을 방지합니다.
 */
@Slf4j
@Service
public class BlacklistServiceImpl implements BlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String LOGOUT_VALUE = "logout";

    private final RedisTemplate<String, Object> template;
    private final JwtProvider jwtProvider;

    /**
     * 생성자 주입 - Redis 템플릿과 JWT 유틸 주입
     *
     * @param template     블랙리스트용 RedisTemplate
     * @param jwtProvider  JWT 유틸리티 (만료 시간 계산용)
     */
    public BlacklistServiceImpl(
            @Qualifier("accessTokenBlacklistRedisTemplate") RedisTemplate<String, Object> template,
            JwtProvider jwtProvider
    ) {
        this.template = template;
        this.jwtProvider = jwtProvider;
    }

    /**
     * 주어진 AccessToken을 Redis 블랙리스트에 등록합니다.
     * <p>
     * Redis key: {@code blacklist:{token}}<br>
     * Value: {@code "logout"}<br>
     * TTL: access token의 남은 만료 시간(ms)
     *
     * @param token 블랙리스트에 등록할 access token
     */
    @Override
    public void addBlacklist(String token) {
        long ttl = jwtProvider.getRemainingExpiration(token);

        template.opsForValue().set(
                BLACKLIST_PREFIX + token,
                LOGOUT_VALUE,
                ttl,
                TimeUnit.MILLISECONDS
        );

        log.debug("[BlacklistService] accessToken 블랙리스트 등록 - key={}, ttl(ms)={}",
                BLACKLIST_PREFIX + token, ttl);
    }
}
