package com.nhnacademy.service.refresh_token.impl;

import com.nhnacademy.service.blacklist.BlacklistService;
import com.nhnacademy.token.exception.InvalidRefreshTokenException;
import com.nhnacademy.token.exception.RefreshTokenNotFoundException;
import com.nhnacademy.token.provider.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class RefreshTokenServiceImplTest {
    @Mock
    RedisTemplate<String, Object> template;

    @Mock
    ValueOperations<String, Object> valueOperations;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    BlacklistService blacklistService;

    @InjectMocks
    RefreshTokenServiceImpl refreshTokenService;

    @Test
    @DisplayName("refresh token 추가 테스트")
    void setRefreshTokenTest() {
        String refreshToken = "refresh_token";
        String userId = "user_id";
        long ttl = 3600000L;

        when(jwtProvider.getRefreshTokenValidity()).thenReturn(ttl);
        when(template.opsForValue()).thenReturn(valueOperations);

        refreshTokenService.setRefreshToken(refreshToken, userId);

        verify(template.opsForValue(), times(1)).set(
                "refreshToken:user_id",
                "refresh_token",
                ttl,
                TimeUnit.MILLISECONDS
        );
    }

    @Test
    @DisplayName("refresh token 삭제 테스트")
    void removeRefreshTokenTest() {
        String accessToken = "access_token";
        String userId = "user_id";

        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn(userId);

        refreshTokenService.removeRefreshToken(accessToken);

        verify(template, times(1)).delete("refreshToken:user_id");
    }

    @Test
    @DisplayName("access token 재발급 테스트")
    void successReissueAccessTokenTest() {
        String accessToken = "access_token";
        String userId = "user_id";
        String refreshToken = "refresh_token";
        String newAccessToken = "new_access_token";

        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn(userId);
        when(template.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(refreshToken);
        when(jwtProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtProvider.createAccessToken(anyString())).thenReturn(newAccessToken);

        assertEquals(newAccessToken, refreshTokenService.reissueAccessToken(accessToken));

        verify(blacklistService, times(1)).addBlacklist(accessToken);
    }

    @Test
    @DisplayName("access token 재발급 실패 테스트 : refresh token이 null")
    void failedReissueAccessTokenTestWithRefreshIsNull() {
        String accessToken = "access_token";
        String userId = "user_id";

        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn(userId);
        when(template.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThrows(RefreshTokenNotFoundException.class, () -> refreshTokenService.reissueAccessToken(accessToken));
    }

    @Test
    @DisplayName("access token 재발급 실패 테스트 : refresh token이 빈 문자열")
    void failedReissueAccessTokenTestWithRefreshIsBlank() {
        String accessToken = "access_token";
        String userId = "user_id";
        String refreshToken = "";

        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn(userId);
        when(template.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(refreshToken);

        assertThrows(RefreshTokenNotFoundException.class, () -> refreshTokenService.reissueAccessToken(accessToken));
        verifyNoMoreInteractions(blacklistService);
    }

    @Test
    @DisplayName("access token 재발급 실패 테스트 : refresh token이 유효하지 않음")
    void failedReissueAccessTokenTestWithInvalidRefreshToken() {
        String accessToken = "access_token";
        String userId = "user_id";
        String refreshToken = "invalid_refresh_token";

        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn(userId);
        when(template.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(refreshToken);
        when(jwtProvider.validateRefreshToken(refreshToken)).thenReturn(false);

        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.reissueAccessToken(accessToken));
        verifyNoMoreInteractions(blacklistService);
    }
}