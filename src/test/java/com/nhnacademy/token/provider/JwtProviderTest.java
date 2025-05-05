package com.nhnacademy.token.provider;

import com.common.AESUtil;
import com.nhnacademy.token.exception.FailCreateAccessTokenException;
import com.nhnacademy.token.exception.TokenException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
    @DisplayName("init() 실행 시 key와 parser가 초기화되어야 한다")
    void shouldInitializeKeyAndParser_whenInitIsCalled() {
        Key key = (Key) ReflectionTestUtils.getField(jwtProvider, "key");
        JwtParser parser = (JwtParser) ReflectionTestUtils.getField(jwtProvider, "parser");

        assertNotNull(key);
        assertNotNull(parser);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "short-secret"})
    @DisplayName("jwtSecretKey가 null, 빈 문자열, 짧은 값이면 예외가 발생해야 한다")
    void shouldThrowException_whenJwtSecretKeyIsInvalid(String invalidSecretKey) {
        ReflectionTestUtils.setField(jwtProvider, "jwtSecretKey", invalidSecretKey);
        assertThrows(IllegalStateException.class, jwtProvider::init);
    }

    @Test
    @DisplayName("유효한 사용자 ID로 AccessToken을 발급할 수 있어야 한다")
    void shouldCreateAccessToken_givenValidUserId() {
        String userId = "test@nhnacademy.com";

        String token = jwtProvider.createAccessToken(userId);

        assertNotNull(token);
    }

    @Test
    @DisplayName("암호화 실패 시 FailCreateAccessTokenException 예외가 발생해야 한다")
    void shouldThrowFailCreateAccessTokenException_whenEncryptionFails() {
        String userId = "test@nhnacademy.com";

        when(aesUtil.encrypt(anyString())).thenThrow(new RuntimeException("암호화 실패"));

        assertThrows(FailCreateAccessTokenException.class, () -> jwtProvider.createAccessToken(userId));
    }

    @Test
    @DisplayName("RefreshToken을 정상적으로 생성할 수 있어야 한다")
    void shouldCreateRefreshTokenSuccessfully() {
        String token = jwtProvider.createRefreshToken();

        assertNotNull(token);
    }

    @Test
    @DisplayName("AccessToken에서 사용자 ID를 추출할 수 있어야 한다")
    void shouldExtractUserIdFromAccessToken() {
        String userId = "zzw123@naver.com";
        String token = jwtProvider.createAccessToken(userId);

        String savedId = jwtProvider.getUserIdFromToken(token);

        assertEquals(userId, savedId);
    }

    @Test
    @DisplayName("null 토큰에서 사용자 ID 추출 시 TokenException이 발생해야 한다")
    void shouldThrowTokenException_whenTokenIsNullForUserIdExtraction() {
        assertThrows(TokenException.class, () -> jwtProvider.getUserIdFromToken(null));
    }

    @Test
    @DisplayName("AccessToken과 RefreshToken에서 만료 시각을 추출할 수 있어야 한다")
    void shouldExtractExpirationTimeFromTokens() {
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
    @DisplayName("null 토큰에서 만료 시각 추출 시 TokenException이 발생해야 한다")
    void shouldThrowTokenException_whenTokenIsNullForExpirationExtraction() {
        assertThrows(TokenException.class, () -> jwtProvider.getExpiredAtFromToken(null));
    }

    @Test
    @DisplayName("토큰에서 남은 만료 시간을 추출할 수 있어야 한다")
    void shouldReturnRemainingExpirationTimeFromTokens() {
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
    @DisplayName("null 토큰에서 남은 시간 추출 시 TokenException이 발생해야 한다")
    void shouldThrowTokenException_whenTokenIsNullForRemainingExpiration() {
        assertThrows(TokenException.class, () -> jwtProvider.getRemainingExpiration(null));
    }

    @Test
    @DisplayName("RefreshToken 유효 시간(ms)을 반환해야 한다")
    void shouldReturnCorrectRefreshTokenValidityInMilliseconds() {
        assertEquals(7 * 24 * 60 * 60 * 1000L, jwtProvider.getRefreshTokenValidity());
    }

    @Test
    @DisplayName("유효한 RefreshToken이면 true를 반환해야 한다")
    void shouldReturnTrue_whenRefreshTokenIsValid() {
        String refreshToken = jwtProvider.createRefreshToken();

        assertTrue(jwtProvider.validateRefreshToken(refreshToken));
    }

    @Test
    @DisplayName("서명 검증에 실패한 RefreshToken이면 false를 반환해야 한다")
    void shouldReturnFalse_whenRefreshTokenHasInvalidSignature() {
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