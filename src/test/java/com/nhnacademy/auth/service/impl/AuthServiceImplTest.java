package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.token.exception.InvalidRefreshTokenException;
import com.nhnacademy.token.exception.RefreshTokenNotFoundException;
import com.nhnacademy.token.provider.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@Slf4j
@ExtendWith(SpringExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAdapter userAdapter;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserSignInRequest userSignInRequest;

    @BeforeEach
    void setUp() {
        userSignInRequest = new UserSignInRequest("auth@email.com", "api12345!");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(jwtProvider.createAccessToken(Mockito.anyString())).thenReturn("accessToken");
        when(jwtProvider.createRefreshToken()).thenReturn("refreshToken");
    }

    @Test
    @DisplayName("user adaptor sign in 성공")
    void successSignIn() {
        Mockito.when(userAdapter.loginUser(Mockito.any(UserSignInRequest.class))).thenReturn(ResponseEntity.ok().build());

        String accessToken = authService.signIn(userSignInRequest);

        assertEquals("accessToken", accessToken);
    }

    @Test
    @DisplayName("user adaptor sign in 실패")
    void failedSignIn() {
        Mockito.when(userAdapter.loginUser(Mockito.any(UserSignInRequest.class))).thenReturn(ResponseEntity.badRequest().build());

        assertThrows(FailSignInException.class, () -> authService.signIn(userSignInRequest));
    }

    @Test
    @DisplayName("sign out test")
    void signOut() {
        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn("userid");
        when(jwtProvider.getRemainingExpiration(anyString())).thenReturn(1234L);

        authService.signOut("accessToken");

        verify(redisTemplate, times(1)).opsForValue();
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("reissue access token")
    void reissueAccessToken() {
        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn("userid");

        when(valueOperations.get("refreshToken:userid")).thenReturn("refreshToken");

        when(jwtProvider.validateRefreshToken(anyString())).thenReturn(true);

        when(jwtProvider.createAccessToken(anyString())).thenReturn("newAccessToken");

        String newAccessToken = authService.reissueAccessToken("accessToken");

        assertEquals("newAccessToken", newAccessToken);
    }

    @Test
    @DisplayName("failed reissue access token: 저장된 refresh token 없음")
    void failedReissueAccessToken() {
        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn("userid");

        when(valueOperations.get("refreshToken:userid")).thenReturn(null);

        assertThrows(RefreshTokenNotFoundException.class, () -> authService.reissueAccessToken("accessToken"));
    }

    @Test
    @DisplayName("failed reissue access token: 저장된 refresh token 없음")
    void failedReissueAccessToken2() {
        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn("userid");

        when(valueOperations.get("refreshToken:userid")).thenReturn("");

        assertThrows(RefreshTokenNotFoundException.class, () -> authService.reissueAccessToken("accessToken"));
    }

    @Test
    @DisplayName("failed reissue access token: 유효하지 않은 refresh token")
    void failedReissueAccessToken3() {
        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn("userid");

        when(valueOperations.get("refreshToken:userid")).thenReturn("refreshToken");

        when(jwtProvider.validateRefreshToken(anyString())).thenReturn(false);

        assertThrows(InvalidRefreshTokenException.class, () -> authService.reissueAccessToken("accessToken"));
    }
}