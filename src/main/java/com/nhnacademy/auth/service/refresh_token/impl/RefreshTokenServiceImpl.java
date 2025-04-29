package com.nhnacademy.auth.service.refresh_token.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.service.blacklist.BlacklistService;
import com.nhnacademy.auth.service.refresh_token.RefreshTokenService;
import com.nhnacademy.token.exception.InvalidRefreshTokenException;
import com.nhnacademy.token.exception.RefreshTokenNotFoundException;
import com.nhnacademy.token.provider.JwtProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    private final RedisTemplate<String, Object> template;
    private final JwtProvider jwtProvider;
    private final BlacklistService blacklistService;

    public RefreshTokenServiceImpl(
            JwtProvider jwtProvider,
            BlacklistService blacklistService,
            @Qualifier("refreshTokenRedisTemplate") RedisTemplate<String, Object> template
    ) {
        this.jwtProvider = jwtProvider;
        this.blacklistService = blacklistService;
        this.template = template;
    }

    @Override
    public void setRefreshToken(String refreshToken, String userId) {
        template.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                jwtProvider.getRefreshTokenValidity(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void removeRefreshToken(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);
        template.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    @Override
    public String reissueAccessToken(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);
        String refreshToken = (String) template.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenNotFoundException();
        }

        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        // AccessToken 재발급
        String newAccessToken = jwtProvider.createAccessToken(userId);

        blacklistService.addBlacklist(accessToken);

        return newAccessToken;
    }
}
