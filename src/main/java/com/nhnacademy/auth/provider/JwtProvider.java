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

/**
 * JwtProvider 클래스는 JWT(Json Web Token) 생성 및 파싱을 담당하는 서비스입니다.
 * <p>
 * 이 클래스는 액세스 토큰 및 리프레시 토큰을 생성하고, 토큰에서 사용자 ID와 만료 시간을 추출하는 기능을 제공합니다.
 * 또한, 토큰의 남은 유효 시간을 계산하는 기능도 제공합니다.
 * </p>
 */
@Component
public class JwtProvider {
    private final Key key;
    private final AESUtil aesUtil;
    // AccessToken 유효시간 = 1시간
    private final long accessTokenValidity = 60 * 60 * 1000L;
    // RefreshToken 유효시간 = 일주일
    private final long refreshTokenValidity = 7 * 24 * 60  * 60 * 1000L;

    /**
     * JwtProvider의 생성자입니다.
     *
     * @param jwtSecretKey JWT 서명에 사용할 비밀 키
     * @param aesUtil AES 암호화 및 복호화 유틸리티
     */
    public JwtProvider(@Value("${jwt.secret}") String jwtSecretKey,
                       AESUtil aesUtil) {
        this.key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
        this.aesUtil = aesUtil;
    }

    /**
     * 액세스 토큰을 생성하는 메소드입니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 액세스 토큰
     */
    public String createAccessToken(String userId) {
        return createToken(userId, accessTokenValidity);
    }

    /**
     * 리프레시 토큰을 생성하는 메소드입니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    public String createRefreshToken(String userId) {
        return createToken(userId, refreshTokenValidity);
    }

    /**
     * 사용자 ID와 유효시간을 기반으로 JWT 토큰을 생성하는 공통 메소드입니다.
     *
     * @param userId 사용자 ID
     * @param validity 토큰의 유효시간(밀리초 단위)
     * @return 생성된 JWT 토큰
     * @throws IllegalStateException 토큰 생성 중 오류가 발생한 경우
     */
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

    /**
     * JWT 토큰에서 사용자 ID를 추출하는 메소드입니다.
     *
     * @param token JWT 토큰
     * @return 추출된 사용자 ID
     * @throws IllegalStateException 토큰에서 사용자 ID를 추출하는 중 오류가 발생한 경우
     */
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

    /**
     * JWT 토큰에서 만료 시간을 추출하는 메소드입니다.
     *
     * @param token JWT 토큰
     * @return 추출된 만료 시간 (yyyy-MM-dd HH:mm:ss 형식)
     */
    public String getExpiredAtFromToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key) //보안상 byte 배열로 권장
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("expired_at", String.class);
    }

    /**
     * JWT 토큰의 만료 예정 시간을 계산하는 메소드입니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 남은 유효시간 (밀리초 단위)
     */
    public long getRemainingExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}
