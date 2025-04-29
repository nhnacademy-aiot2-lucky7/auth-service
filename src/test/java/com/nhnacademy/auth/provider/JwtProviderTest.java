package com.nhnacademy.auth.provider;

import com.common.AESUtil;
import com.nhnacademy.token.exception.FailCreateAccessTokenException;
import com.nhnacademy.token.exception.TokenException;
import com.nhnacademy.token.provider.JwtProvider;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
class JwtProviderTest {
    @Autowired
    JwtProvider jwtProvider;

    @MockitoSpyBean
    AESUtil aesUtil;

    @Test
    @DisplayName("init Test")
    void initializeTest() {
        Key key = (Key) ReflectionTestUtils.getField(jwtProvider, "key");
        JwtParser parser = (JwtParser) ReflectionTestUtils.getField(jwtProvider, "parser");

        assertNotNull(key);
        assertNotNull(parser);
    }

    @Test
    @DisplayName("init Test: 실패 테스트 jwt secret key is null")
    void failedInitializeTest1() {
        ReflectionTestUtils.setField(jwtProvider, "jwtSecretKey", null);
        assertThrows(IllegalStateException.class, () -> jwtProvider.init());
    }

    @Test
    @DisplayName("init Test: 실패 테스트 jwt secret key is empty")
    void failedInitializeTest2() {
        ReflectionTestUtils.setField(jwtProvider, "jwtSecretKey", "");
        assertThrows(IllegalStateException.class, () -> jwtProvider.init());
    }

    @Test
    @DisplayName("init Test: 실패 테스트 jwt secret key is too short")
    void failedInitializeTest3() {
        ReflectionTestUtils.setField(jwtProvider, "jwtSecretKey", "short-secret");
        assertThrows(IllegalStateException.class, () -> jwtProvider.init());
    }

    @Test
    @DisplayName("AccessToken 발급 test")
    void createAccessTokenTest() {
        String userId = "test@nhnacademy.com";

        String token = jwtProvider.createAccessToken(userId);

        assertNotNull(token);
    }

    @Test
    @DisplayName("AccessToken 발급 실패 테스트 encrypt 중 에러 발생")
    void failedCreateAccessTokenTest() {
        String userId = "test@nhnacademy.com";

        when(aesUtil.encrypt(anyString())).thenThrow(new RuntimeException("암호화 실패"));

        assertThrows(FailCreateAccessTokenException.class, () -> jwtProvider.createAccessToken(userId));
    }

    @Test
    @DisplayName("RefreshToken 발급 test")
    void createRefreshTokenTest() {
        String token = jwtProvider.createRefreshToken();

        assertNotNull(token);
    }

    @Test
    @DisplayName("token에서 user id 추출")
    void getUserIdFromTokenTest() {
        String userId = "zzw123@naver.com";
        String token = jwtProvider.createAccessToken(userId);

        String savedId = jwtProvider.getUserIdFromToken(token);

        assertEquals(userId, savedId);
    }

    @Test
    @DisplayName("token에서 user id 추출 실패 token is null")
    void failedGetUserIdFromTokenTest() {
        assertThrows(TokenException.class, () -> jwtProvider.getUserIdFromToken(null));
    }

    @Test
    @DisplayName("token에서 만료시각 추출 test")
    void getExpiredAtFromTokenTest() {
        String userId = "zzw123@naver.com";
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken();
        Long millis = System.currentTimeMillis();

        Long accessTokenExpiration = jwtProvider.getExpiredAtFromToken(accessToken);
        Long refreshTokenExpiration = jwtProvider.getExpiredAtFromToken(refreshToken);

        assertNotNull(accessTokenExpiration);
        assertNotNull(refreshTokenExpiration);

        assertTrue(accessTokenExpiration - millis < 60 * 60 * 1000L);
        assertTrue(refreshTokenExpiration - millis < 7 * 24 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("token에서 만료시각 추출 실패 token is null")
    void failedGetExpiredAtFromTokenTest() {
        assertThrows(TokenException.class, () -> jwtProvider.getExpiredAtFromToken(null));
    }

    @Test
    @DisplayName("만료까지 남은 시간 추출")
    void getRemainingExpirationTest() {
        String userId = "zzw123@naver.com";
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken();

        Long accessTokenExpiration = jwtProvider.getRemainingExpiration(accessToken);
        Long refreshTokenExpiration = jwtProvider.getRemainingExpiration(refreshToken);

        assertNotNull(accessTokenExpiration);
        assertNotNull(refreshTokenExpiration);

        assertTrue(accessTokenExpiration < 60 * 60 * 1000L);
        assertTrue(refreshTokenExpiration < 7 * 24 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("만료까지 남은 시간 추출 실패 token is null")
    void failedGetRemainingExpirationTest() {
        assertThrows(TokenException.class, () -> jwtProvider.getRemainingExpiration(null));
    }

    @Test
    @DisplayName("refresh token 만료 시간 조회")
    void getRefreshTokenValidity() {
        assertEquals(7 * 24 * 60 * 60 * 1000L, jwtProvider.getRefreshTokenValidity());
    }

    @Test
    @DisplayName("refresh token signature 성공 검증")
    void successValidateRefreshToken() {
        String refreshToken = jwtProvider.createRefreshToken();

        assertTrue(jwtProvider.validateRefreshToken(refreshToken));
    }

    @Test
    @DisplayName("refresh token signature 실패 검증")
    void failedValidateRefreshTokenTest() {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + jwtProvider.getRefreshTokenValidity());

        String refreshToken = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiredAt)
                .signWith(Keys.hmacShaKeyFor("This-Secret-Key-Is-Test-Secret-Key222".getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtProvider.validateRefreshToken(refreshToken));
    }
}