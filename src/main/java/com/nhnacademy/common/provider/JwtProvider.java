package com.nhnacademy.common.provider;

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
    // AccessToken 유효시간 = 1시간
    private final long accessTokenValidity = 60 * 60 * 1000L;
    // RefreshToken 유효시간 = 일주일
    private final long refreshTokenValidity = 7 * 24 * 60  * 60 * 1000L;

    public JwtProvider(@Value("jwt.secret")String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String userEmail) {
        return createToken(userEmail, accessTokenValidity);
    }

    public String createRefreshToken(String userEmail) {
        return createToken(userEmail, refreshTokenValidity);
    }

    // 테스트용
    public String createTestToken(String userEmail) {
        return createToken(userEmail, 5000);
    }

    private String createToken(String userEmail, long validity) {
        Date now = new Date();  // 현재시간
        Date expiredAt = new Date(now.getTime() + validity);  // 만료시간

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expiredAtStr = sdf.format(expiredAt);

        return Jwts.builder()
                .claim("user_id", userEmail)
                .claim("expired_at", expiredAtStr)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key) //보안상 byte 배열로 권장
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("user_id", String.class);
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

/*    // 테스트
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }*/
}
