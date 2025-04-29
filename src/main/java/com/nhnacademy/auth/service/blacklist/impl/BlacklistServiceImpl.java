package com.nhnacademy.auth.service.blacklist.impl;

import com.nhnacademy.auth.service.blacklist.BlacklistService;
import com.nhnacademy.token.provider.JwtProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BlacklistServiceImpl implements BlacklistService {
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String LOGOUT_VALUE = "logout";

    private final RedisTemplate<String, Object> template;
    private final JwtProvider jwtProvider;

    public BlacklistServiceImpl(
            @Qualifier("accessTokenBlacklistRedisTemplate") RedisTemplate<String, Object> template,
            JwtProvider jwtProvider
    ) {
        this.template = template;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void addBlacklist(String token) {
        long expiredAccessTokenTtl = jwtProvider.getRemainingExpiration(token);

        template.opsForValue().set(
                BLACKLIST_PREFIX + token,
                LOGOUT_VALUE,
                expiredAccessTokenTtl,
                TimeUnit.MILLISECONDS
        );
    }
}
