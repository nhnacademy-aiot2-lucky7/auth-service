package com.nhnacademy.common.provider;

import com.nhnacademy.auth.provider.JwtProvider;
import com.nhnacademy.auth.util.AESUtil;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class JwtProviderTest {

    private JwtProvider jwtProvider;

    @Value("${aes.secret}")
    private String aesSecretKey;

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @BeforeEach
    void setup() {
        AESUtil aesUtil = new AESUtil(aesSecretKey);
        jwtProvider =  new JwtProvider(jwtSecretKey, aesUtil);
    }

    @Test
    @DisplayName("AccessToken 발급 확인")
    void createAccessToken() {
        String userId = "test@nhnacademy.com";
        String token = jwtProvider.createAccessToken(userId);
        String expired_at = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("expired_at", String.class);

        Assertions.assertNotNull(token);
        Assertions.assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
        Assertions.assertEquals(expired_at, jwtProvider.getExpiredAtFromToken(token));
    }

    @Test
    @DisplayName("RefreshToken 발급 확인")
    void createRefreshToken() {
        String userId = "test@nhnacademy.com";
        String token = jwtProvider.createRefreshToken(userId);
        String expired_at = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("expired_at", String.class);

        Assertions.assertNotNull(token);
        Assertions.assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
        Assertions.assertEquals(expired_at, jwtProvider.getExpiredAtFromToken(token));
    }
}