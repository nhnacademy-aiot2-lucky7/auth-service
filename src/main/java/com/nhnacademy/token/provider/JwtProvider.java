package com.nhnacademy.token.provider;

import com.common.AESUtil;
import com.nhnacademy.token.exception.FailCreateAccessTokenException;
import com.nhnacademy.token.exception.FailCreateRefreshTokenException;
import com.nhnacademy.token.exception.TokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final AESUtil aesUtil;

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    private JwtParser parser;
    private Key key;

    private static final String CLAIM_USER_ID = "user_id";

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    @PostConstruct
    public void init() {
        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            throw new IllegalStateException("❌ jwt.secret 설정이 누락되었습니다. application.properties 또는 환경변수를 확인하세요.");
        }
        if (jwtSecretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("❌ jwt.secret 값이 너무 짧습니다. 최소 256비트(32바이트) 이상이어야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String createAccessToken(String userId) {
        try {
            String encryptedUserId = aesUtil.encrypt(userId);
            return createToken(ACCESS_TOKEN_DURATION, encryptedUserId);
        } catch (Exception e) {
            throw new FailCreateAccessTokenException();
        }
    }

    public String createRefreshToken() {
        try {
            return createToken(REFRESH_TOKEN_DURATION, null);
        } catch (Exception e) {
            throw new FailCreateRefreshTokenException();
        }
    }

    private String createToken(Duration duration, String encryptedUserId) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + duration.toMillis()))
                .signWith(key, SignatureAlgorithm.HS256);

        if (encryptedUserId != null) {
            builder.claim(CLAIM_USER_ID, encryptedUserId);
        }

        return builder.compact();
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            return aesUtil.decrypt(claims.get(CLAIM_USER_ID, String.class));
        } catch (Exception e) {
            throw new TokenException("토큰에서 사용자 ID 복호화 중 예외 발생", e);
        }
    }

    public Long getExpiredAtFromToken(String token) {
        return extractClaims(token).getExpiration().getTime();
    }

    public Long getRemainingExpiration(String token) {
        return getExpiredAtFromToken(token) - System.currentTimeMillis();
    }

    public Long getRefreshTokenValidity() {
        return REFRESH_TOKEN_DURATION.toMillis();
    }

    /**
     * refresh token의 서명만 검증. 만료 여부는 무시한다.
     */
    public boolean validateRefreshToken(String token) {
        try {
            parser.parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("Refresh token parsing failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractClaims(String token) {
        try {
            return parser.parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new TokenException("토큰 파싱 중 예외 발생", e);
        }
    }
}
