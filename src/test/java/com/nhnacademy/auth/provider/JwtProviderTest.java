package com.nhnacademy.auth.provider;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    Dotenv dotenv;

    @Test
    @DisplayName("AccessToken 발급 확인")
    void createAccessToken() {
        String userId = "test@nhnacademy.com";
        String token = jwtProvider.createAccessToken(userId);

        String getExpiredAt = Jwts.parserBuilder()
                .setSigningKey(Objects.requireNonNull(dotenv.get("JWT_SECRET")).getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("expired_at", String.class);

        Assertions.assertNotNull(token);
        Assertions.assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
        Assertions.assertEquals(getExpiredAt, jwtProvider.getExpiredAtFromToken(token));
    }

    @Test
    @DisplayName("RefreshToken 발급 확인")
    void createRefreshToken() {
        String userId = "test@nhnacademy.com";
        String token = jwtProvider.createRefreshToken(userId);
        String getExpiredAt = Jwts.parserBuilder()
                .setSigningKey(Objects.requireNonNull(dotenv.get("JWT_SECRET")).getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("expired_at", String.class);

        Assertions.assertNotNull(token);
        Assertions.assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
        Assertions.assertEquals(getExpiredAt, jwtProvider.getExpiredAtFromToken(token));
    }

    @Test
    @DisplayName("만료 예정 시간 확인")
    void testGetRemainingExpiration() {
        String userId = "test@nhnacademy.com";
        String accessToken = jwtProvider.createAccessToken(userId);

        long remaining = jwtProvider.getRemainingExpiration(accessToken);

        Assertions.assertTrue(remaining > 0);
        Assertions.assertTrue(remaining <= 60 * 60 * 1000L);
    }
}