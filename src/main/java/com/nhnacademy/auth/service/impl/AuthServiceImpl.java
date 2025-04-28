package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.service.AuthService;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.common.exception.UnauthorizedException;
import com.nhnacademy.token.exception.InvalidRefreshTokenException;
import com.nhnacademy.token.exception.RefreshTokenNotFoundException;
import com.nhnacademy.token.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String LOGOUT_VALUE = "logout";
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    private final UserAdapter userAdapter;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String signIn(UserSignInRequest userSignInRequest) {
        ResponseEntity<Void> responseEntity = userAdapter.loginUser(userSignInRequest);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // 로그인 성공 -> 토큰 생성
            String userId = userSignInRequest.getUserEmail();

            return createAccessAndStoreRefreshTokenToRedis(userId);
        } else {
            throw new FailSignInException(responseEntity.getStatusCode().value());
        }
    }

    @Override
    public void signOut(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);

        // AccessToken 블랙리스트 등록
        long expiredAccessTokenTtl = jwtProvider.getRemainingExpiration(accessToken);
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, LOGOUT_VALUE, expiredAccessTokenTtl, TimeUnit.MILLISECONDS);

        // refresh token 삭제
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    private String createAccessAndStoreRefreshTokenToRedis(String userId) {
        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken();
        // Redis에 refreshToken 저장 (유효기간 설정)
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, refreshToken, jwtProvider.getRefreshTokenValidity(), TimeUnit.MILLISECONDS);

        return accessToken;
    }

    @Override
    public String reissueAccessToken(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);

        String refreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenNotFoundException();
        }

        if(!jwtProvider.validateRefreshToken(refreshToken)){
            throw new InvalidRefreshTokenException();
        }

        // AccessToken 재발급
        String newAccessToken = jwtProvider.createAccessToken(userId);

        // 만료 예정 AccessToken의 ttl
        long expiredAccessTokenTTL = jwtProvider.getRemainingExpiration(accessToken);
        // 만료 예정 AccessToken 블랙리스트 등록
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, LOGOUT_VALUE, expiredAccessTokenTTL, TimeUnit.MILLISECONDS);

        return newAccessToken;
    }
}
