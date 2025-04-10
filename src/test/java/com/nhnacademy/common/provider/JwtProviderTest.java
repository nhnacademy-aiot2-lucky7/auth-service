package com.nhnacademy.common.provider;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setup() {

        String secretKey = "This-Secret-Key-Is-Test-Secret-Key";
        jwtProvider =  new JwtProvider(secretKey);
    }

    @Test
    @DisplayName("AccessToken 발급 확인")
    void createAccessToken() {
        String userId = "test@nhnacademy.com";

        String token = jwtProvider.createAccessToken(userId);
        assertNotNull(token);
        String expired_at = Jwts.parserBuilder().setSigningKey("This-Secret-Key-Is-Test-Secret-Key".getBytes(StandardCharsets.UTF_8)).build().parseClaimsJws(token).getBody().get("expired_at", String.class);
        log.info("expired_at: ", expired_at);
        assertTrue(jwtProvider.validateToken(token));
        assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("RefreshToken 발급 확인")
    void createRefreshToken() {
        String userId = "test@nhnacademy.com";

        String token = jwtProvider.createRefreshToken(userId);
        assertNotNull(token);

        assertTrue(jwtProvider.validateToken(token));
        assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("유효하지 않은 토큰 오류 발생 확인")
    void invalidTokenShouldFailValidation() {
        String invalidToken = "this.is.not.a.valid.token";
        assertFalse(jwtProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("임시로 짧은 유효시간의 토큰 발급 후 만료 확인")
    void validateToken() throws InterruptedException {
        String userId = "test@nhnacademy.com";

        String shortTimeToken = jwtProvider.createTestToken(userId);

        assertTrue(jwtProvider.validateToken(shortTimeToken));
        assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(shortTimeToken));

        Thread.sleep(6000);

        assertFalse(jwtProvider.validateToken(shortTimeToken));
    }
}