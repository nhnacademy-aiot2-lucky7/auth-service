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

/**
 * JWT 생성, 파싱, 검증을 담당하는 Provider 클래스입니다.
 * <p>
 * AccessToken은 userId를 암호화하여 포함하며, RefreshToken은 payload 없이 서명만 유지합니다.
 */
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

    /**
     * JWT Provider 초기화 메서드입니다.
     * secret 키가 유효한지 검증하고, 키와 파서를 준비합니다.
     *
     * @throws IllegalStateException 설정 오류 시
     */
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

    /**
     * 사용자 ID를 기반으로 AccessToken을 생성합니다.
     *
     * @param userId 사용자 ID (email)
     * @return 암호화된 access token 문자열
     * @throws FailCreateAccessTokenException 암호화 실패 시
     */
    public String createAccessToken(String userId) {
        try {
            String encryptedUserId = aesUtil.encrypt(userId);
            return createToken(ACCESS_TOKEN_DURATION, encryptedUserId);
        } catch (Exception e) {
            throw new FailCreateAccessTokenException();
        }
    }

    /**
     * RefreshToken을 생성합니다. 사용자 정보 없이 서명만 포함됩니다.
     *
     * @return refresh token 문자열
     * @throws FailCreateRefreshTokenException 생성 실패 시
     */
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

    /**
     * 토큰에서 사용자 ID를 추출하고 복호화합니다.
     *
     * @param token JWT 토큰
     * @return 복호화된 사용자 ID
     * @throws TokenException 복호화 실패 또는 파싱 실패 시
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            return aesUtil.decrypt(claims.get(CLAIM_USER_ID, String.class));
        } catch (Exception e) {
            throw new TokenException("토큰에서 사용자 ID 복호화 중 예외 발생", e);
        }
    }

    /**
     * 토큰 만료 시각(epoch ms)을 반환합니다.
     *
     * @param token JWT 토큰
     * @return 만료 시간 (밀리초)
     * @throws TokenException 파싱 실패 시
     */
    public Long getExpiredAtFromToken(String token) {
        return extractClaims(token).getExpiration().getTime();
    }

    /**
     * 토큰 만료까지 남은 시간을 반환합니다.
     *
     * @param token JWT 토큰
     * @return 남은 시간 (밀리초)
     */
    public Long getRemainingExpiration(String token) {
        return getExpiredAtFromToken(token) - System.currentTimeMillis();
    }

    /**
     * refresh token의 유효 기간을 반환합니다.
     *
     * @return refresh token TTL (밀리초)
     */
    public Long getRefreshTokenValidity() {
        return REFRESH_TOKEN_DURATION.toMillis();
    }

    /**
     * RefreshToken의 서명을 검증합니다.
     * <p>만료 여부는 검사하지 않고 서명 유효성만 확인합니다.
     *
     * @param token refresh token
     * @return 유효하면 true, 실패 시 false
     */
    public boolean validateRefreshToken(String token) {
        try {
            parser.parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("[JwtProvider] RefreshToken 유효성 검증 실패: {}", e.getMessage());
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
