package com.nhnacademy.auth.provider;

import com.common.AESUtil;
import com.nhnacademy.token.exception.FailCreateAccessTokenException;
import com.nhnacademy.token.exception.FailCreateRefreshTokenException;
import com.nhnacademy.token.exception.JwtSecretKeyMissingException;
import com.nhnacademy.token.exception.TokenException;
import com.nhnacademy.token.provider.JwtProvider;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    Dotenv dotenv;

    @Autowired
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
    @DisplayName("init Test: 실패 테스트")
    void initializeTest2() {
        Dotenv dummyDotenv = mock(Dotenv.class);
        AESUtil dummyAesUtil = mock(AESUtil.class);
        Environment dummyEnv = mock(Environment.class);

        JwtProvider failJwtProvider = new JwtProvider(dummyDotenv, dummyEnv, dummyAesUtil);

        assertThrows(JwtSecretKeyMissingException.class, failJwtProvider::init);
    }

    @Test
    @DisplayName("AccessToken 발급 확인")
    void createAccessToken() {
        String userId = "test@nhnacademy.com";
        String token = jwtProvider.createAccessToken(userId);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Objects.requireNonNull(dotenv.get("JWT_SECRET")).getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(token);
        assertEquals("test@nhnacademy.com", aesUtil.decrypt(claims.get("user_id").toString()));
        Assertions.assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("AccessToken 발급 실패")
    void failCreateAccessToken() {
        Dotenv dummyDotenv = mock(Dotenv.class);
        AESUtil dummyAesUtil = mock(AESUtil.class);
        Environment dummyEnv = mock(Environment.class);

        JwtProvider dummyJwtProvider = new JwtProvider(dummyDotenv, dummyEnv, dummyAesUtil);
        jwtProvider.init();

        when(dummyAesUtil.encrypt(anyString())).thenThrow(new RuntimeException("암호화 실패"));

        assertThrows(FailCreateAccessTokenException.class, () -> dummyJwtProvider.createAccessToken("userId"));
    }

    @Test
    @DisplayName("RefreshToken 발급 확인")
    void createRefreshToken() {
        String token = jwtProvider.createRefreshToken();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Objects.requireNonNull(dotenv.get("JWT_SECRET")).getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(token);
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("refreshToken 발급 실패")
    void failCreateRefreshToken() {
        Dotenv dummyDotenv = mock(Dotenv.class);
        AESUtil dummyAesUtil = mock(AESUtil.class);
        Environment dummyEnv = mock(Environment.class);

        JwtProvider dummyJwtProvider = new JwtProvider(dummyDotenv, dummyEnv, dummyAesUtil);
        jwtProvider.init();

        ReflectionTestUtils.setField(dummyJwtProvider, "key", null);

        assertThrows(FailCreateRefreshTokenException.class, dummyJwtProvider::createRefreshToken);
    }

    @Test
    @DisplayName("token에서 user id 추출")
    void getUserIdFromToken() {
        String userId = "zzw123@naver.com";
        String token = jwtProvider.createAccessToken(userId);

        String savedId = jwtProvider.getUserIdFromToken(token);

        assertEquals(userId, savedId);
    }

    @Test
    @DisplayName("token에서 user id 추출 실패")
    void failGetUserIdFromToken() {
        assertThrows(TokenException.class, () -> jwtProvider.getUserIdFromToken(null));
    }

    @Test
    @DisplayName("token에서 만료시각 추출")
    void getExpiredAtFromToken() {
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
    @DisplayName("token에서 만료시각 추출 실패")
    void failGetExpiredAtFromToken() {
        assertThrows(TokenException.class, () -> jwtProvider.getExpiredAtFromToken(null));
    }


    @Test
    @DisplayName("만료까지 남은 시간 추출")
    void getRemainingExpiration() {
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
    @DisplayName("만료까지 남은 시간 추출")
    void failGetRemainingExpiration() {
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
    void failedValidateRefreshToken() {
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