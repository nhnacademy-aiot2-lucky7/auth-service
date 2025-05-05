package com.nhnacademy.service.refresh_token.impl;

import com.nhnacademy.service.blacklist.BlacklistService;
import com.nhnacademy.service.refresh_token.RefreshTokenService;
import com.nhnacademy.token.exception.InvalidRefreshTokenException;
import com.nhnacademy.token.exception.RefreshTokenNotFoundException;
import com.nhnacademy.token.provider.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RefreshToken 관련 기능을 처리하는 서비스 구현체입니다.
 * <p>
 * 토큰 저장, 삭제, accessToken 재발급 기능을 제공합니다.
 */
@Slf4j
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    private final RedisTemplate<String, Object> template;
    private final JwtProvider jwtProvider;
    private final BlacklistService blacklistService;

    /**
     * 생성자
     *
     * @param jwtProvider JWT 유틸리티
     * @param blacklistService accessToken 블랙리스트 처리용 서비스
     * @param template refresh token 저장용 RedisTemplate
     */
    public RefreshTokenServiceImpl(
            JwtProvider jwtProvider,
            BlacklistService blacklistService,
            @Qualifier("refreshTokenRedisTemplate") RedisTemplate<String, Object> template
    ) {
        this.jwtProvider = jwtProvider;
        this.blacklistService = blacklistService;
        this.template = template;
    }

    /**
     * 주어진 사용자 ID에 대한 refreshToken을 Redis에 저장합니다.
     *
     * @param refreshToken 저장할 토큰
     * @param userId 사용자 ID
     */
    @Override
    public void setRefreshToken(String refreshToken, String userId) {
        long ttl = jwtProvider.getRefreshTokenValidity();

        template.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                ttl,
                TimeUnit.MILLISECONDS
        );

        log.debug("[RefreshTokenService] 저장 완료 - userId={}, ttl(ms)={}", userId, ttl);
    }

    /**
     * accessToken을 기반으로 사용자 ID를 추출하여 해당 사용자의 refreshToken을 Redis에서 삭제합니다.
     *
     * @param accessToken 로그아웃한 사용자의 accessToken
     */
    @Override
    public void removeRefreshToken(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);
        template.delete(REFRESH_TOKEN_PREFIX + userId);

        log.debug("[RefreshTokenService] 삭제 완료 - userId={}", userId);
    }

    /**
     * 주어진 accessToken의 사용자 ID를 기준으로 refreshToken을 조회 및 검증 후, 새 accessToken을 발급합니다.
     * 기존 accessToken은 블랙리스트에 등록됩니다.
     *
     * @param accessToken 기존 access token
     * @return 새로 생성된 access token
     * @throws RefreshTokenNotFoundException refresh token이 없을 경우
     * @throws InvalidRefreshTokenException refresh token이 유효하지 않은 경우
     */
    @Override
    public String reissueAccessToken(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);
        log.debug("[RefreshTokenService] 재발급 시도 - userId={}", userId);

        String refreshToken = (String) template.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("[RefreshTokenService] 없음 - userId={}", userId);
            throw new RefreshTokenNotFoundException();
        }

        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            log.warn("[RefreshTokenService] 유효하지 않음 - userId={}", userId);
            throw new InvalidRefreshTokenException();
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        blacklistService.addBlacklist(accessToken);

        log.debug("[RefreshTokenService] 재발급 완료 - userId={}, newAccessToken={}", userId, newAccessToken);

        return newAccessToken;
    }
}
