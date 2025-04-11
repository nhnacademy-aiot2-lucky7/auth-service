package com.nhnacademy.auth.provider;

import com.nhnacademy.auth.util.AESUtil;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;

import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    private final Key key;
    private final AESUtil aesUtil;
    // AccessToken 유효시간 = 1시간
    private final long accessTokenValidity = 60 * 60 * 1000L;
    // RefreshToken 유효시간 = 일주일
    private final long refreshTokenValidity = 7 * 24 * 60  * 60 * 1000L;

    public JwtProvider(@Value("${jwt.secret}") String jwtSecretKey,
                       AESUtil aesUtil) {
        this.key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        this.aesUtil = aesUtil;
    }

    public String createAccessToken(String userId) {
        return createToken(userId, accessTokenValidity);
    }

    public String createRefreshToken(String userId) {
        return createToken(userId, refreshTokenValidity);
    }

    private String createToken(String userId, long validity) {

        try {
            String encryptedUserId = aesUtil.encrypt(userId);
            Date now = new Date();  // 현재시간
            Date expiredAt = new Date(now.getTime() + validity);  // 만료시간
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String expiredAtStr = sdf.format(expiredAt);

            return Jwts.builder()
                    .claim("user_id", encryptedUserId)
                    .claim("expired_at", expiredAtStr)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + validity))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new IllegalStateException("토큰에서 사용자 ID 암호화 중 예외 발생", e);
        }

    }

    public String getUserIdFromToken(String token) {
        try {
            String encryptedUserId = Jwts.parserBuilder()
                    .setSigningKey(key) //보안상 byte 배열로 권장
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("user_id", String.class);

            return aesUtil.decrypt(encryptedUserId);
        } catch (Exception e) {
            throw new IllegalStateException("토큰에서 사용자 ID 복호화 중 예외 발생", e);
        }
    }

    public String getExpiredAtFromToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key) //보안상 byte 배열로 권장
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("expired_at", String.class);
    }
/*
    // 테스트
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }*/
}
