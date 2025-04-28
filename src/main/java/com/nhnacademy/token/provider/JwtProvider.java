package com.nhnacademy.token.provider;

import com.common.AESUtil;
import com.nhnacademy.token.exception.FailCreateAccessTokenException;
import com.nhnacademy.token.exception.FailCreateRefreshTokenException;
import com.nhnacademy.token.exception.JwtSecretKeyMissingException;
import com.nhnacademy.token.exception.TokenException;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    private final Dotenv dotenv;
    private final Environment env;
    private final AESUtil aesUtil;

    private JwtParser parser;
    private Key key;

    private static final String JWT_SECRET_KEY = "JWT_SECRET";
    private static final String CLAIM_USER_ID = "user_id";

    private static final long ACCESS_TOKEN_VALIDITY = 60 * 60 * 1000L;          // 1시간
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L; // 7일

    public JwtProvider(Dotenv dotenv, Environment env, AESUtil aesUtil) {
        this.dotenv = dotenv;
        this.env = env;
        this.aesUtil = aesUtil;
    }

    @PostConstruct
    public void init() {
        String jwtSecretKey = dotenv.get(JWT_SECRET_KEY);

        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            jwtSecretKey = env.getProperty("jwt.secret");
        }

        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            throw new JwtSecretKeyMissingException();
        }

        key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        parser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String createAccessToken(String userId) {
        try {
            String encryptedUserId = aesUtil.encrypt(userId);
            Date now = new Date();
            Date expiredAt = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

            return Jwts.builder()
                    .claim(CLAIM_USER_ID, encryptedUserId)
                    .setIssuedAt(now)
                    .setExpiration(expiredAt)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new FailCreateAccessTokenException();
        }
    }

    public String createRefreshToken() {
        try {
            Date now = new Date();
            Date expiredAt = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);

            return Jwts.builder()
                    .setIssuedAt(now)
                    .setExpiration(expiredAt)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new FailCreateRefreshTokenException();
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            String encryptedUserId = claims.get(CLAIM_USER_ID, String.class);
            return aesUtil.decrypt(encryptedUserId);
        } catch (Exception e) {
            throw new TokenException("토큰에서 사용자 ID 복호화 중 예외 발생", e);
        }
    }

    public Long getExpiredAtFromToken(String token) {
        try {
            Date expiration = parser.parseClaimsJws(token).getBody().getExpiration();
            return expiration.getTime();
        } catch (Exception e) {
            throw new TokenException("토큰에서 만료 시간 추출 중 예외 발생", e);
        }
    }

    public Long getRemainingExpiration(String token) {
        try {
            Date expiration = parser.parseClaimsJws(token).getBody().getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            throw new TokenException("토큰에서 남은 시간 계산 중 예외 발생", e);
        }
    }

    public Long getRefreshTokenValidity() {
        return REFRESH_TOKEN_VALIDITY;
    }

    /**
     * refresh token의 서명만 검증. 만료 여부는 무시한다.
     *
     * @param token JWT refresh token
     * @return 서명이 유효하면 true, 아니면 false
     */
    public boolean validateRefreshToken(String token) {
        try {
            parser.parseClaimsJws(token); // 서명만 검증됨. 만료돼도 SignatureException 아님.
            return true;
        } catch (Exception e) {
            // 만료, 형식 오류 등은 무시
            log.warn("Refresh token parsing failed: {}", e.getMessage());
            return false;
        }
    }
}
