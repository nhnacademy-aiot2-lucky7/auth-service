package com.nhnacademy.common.provider;

import com.nhnacademy.auth.provider.JwtProvider;
import com.nhnacademy.auth.util.AESUtil;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

@Slf4j
@ActiveProfiles("test")
class JwtProviderTest {

    private JwtProvider jwtProvider;

    private final String aesSecretKey = "6xm70v3kdyoft71r01bevklfq10zxs0d";
    private final String jwtSecretKey = "This-Secret-Key-Is-Test-Secret-Key";

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
        String expired_at = Jwts.parserBuilder().setSigningKey("This-Secret-Key-Is-Test-Secret-Key".getBytes(StandardCharsets.UTF_8)).build().parseClaimsJws(token).getBody().get("expired_at", String.class);

        Assertions.assertNotNull(token);
//        Assertions.assertTrue(jwtProvider.validateToken(token));
        Assertions.assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
        Assertions.assertEquals(expired_at, jwtProvider.getExpiredAtFromToken(token));
    }

    @Test
    @DisplayName("RefreshToken 발급 확인")
    void createRefreshToken() {
        String userId = "test@nhnacademy.com";
        String token = jwtProvider.createRefreshToken(userId);
        String expired_at = Jwts.parserBuilder().setSigningKey("This-Secret-Key-Is-Test-Secret-Key".getBytes(StandardCharsets.UTF_8)).build().parseClaimsJws(token).getBody().get("expired_at", String.class);

        Assertions.assertNotNull(token);
//        Assertions.assertTrue(jwtProvider.validateToken(token));
        Assertions.assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(token));
        Assertions.assertEquals(expired_at, jwtProvider.getExpiredAtFromToken(token));
    }

 /*   @Test
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
    }*/
}