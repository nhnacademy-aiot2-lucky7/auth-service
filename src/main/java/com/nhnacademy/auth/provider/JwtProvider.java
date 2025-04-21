package com.nhnacademy.auth.provider;

import com.nhnacademy.auth.util.AESUtil;
import com.nhnacademy.common.exception.UnauthorizedException;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JwtProvider 클래스는 JWT(Json Web Token) 생성 및 파싱을 담당하는 서비스입니다.
 * <p>
 * 이 클래스는 액세스 토큰 및 리프레시 토큰을 생성하고, 토큰에서 사용자 ID와 만료 시간을 추출하는 기능을 제공합니다.
 * 또한, 토큰의 남은 유효 시간을 계산하는 기능도 제공합니다.
 * </p>
 */
@Slf4j
@Component
public class JwtProvider {
    private final Key key;
    private final AESUtil aesUtil;
    private static final String JWT_SECRET = "JWT_SECRET";
    // AccessToken 유효시간 = 1시간
    private static final long ACCESS_TOKEN_VALIDITY = 60 * 60 * 1000L;
    // RefreshToken 유효시간 = 일주일
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60  * 60 * 1000L;

    /**
     * JwtProvider의 생성자입니다.
     *
     *
     * @param aesUtil AES 암호화 및 복호화 유틸리티
     */
    public JwtProvider(AESUtil aesUtil) {
        this.aesUtil = aesUtil;

        String jwtSecretKey = null;

        // 우선 .env에서 시도
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            jwtSecretKey = dotenv.get(JWT_SECRET);
        } catch (Exception ignored) {
            log.debug(".env파일에서 키 추출 실패. properties파일로 넘어감.");
        }

        // 환경 변수나 시스템 프로퍼티로 fallback
        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            jwtSecretKey = System.getProperty(JWT_SECRET);
            if (jwtSecretKey == null) {
                jwtSecretKey = System.getenv(JWT_SECRET);
            }
        }

        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            throw new UnauthorizedException("JWT_SECRET가 설정되지 않았습니다.");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 액세스 토큰을 생성하는 메소드입니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 액세스 토큰
     */
    public String createAccessToken(String userId) {
        return createToken(userId, ACCESS_TOKEN_VALIDITY);
    }

    /**
     * 리프레시 토큰을 생성하는 메소드입니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    public String createRefreshToken(String userId) {
        return createToken(userId, REFRESH_TOKEN_VALIDITY);
    }

    /**
     * 사용자 ID와 유효시간을 기반으로 JWT 토큰을 생성하는 공통 메소드입니다.
     *
     * @param userId 사용자 ID
     * @param validity 토큰의 유효시간(밀리초 단위)
     * @return 생성된 JWT 토큰
     * @throws UnauthorizedException 토큰 생성 중 오류가 발생한 경우
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
            throw new UnauthorizedException("토큰에서 사용자 ID 암호화 중 예외 발생");
        }

    }

    /**
     * JWT 토큰에서 사용자 ID를 추출하는 메소드입니다.
     *
     * @param token JWT 토큰
     * @return 추출된 사용자 ID
     * @throws UnauthorizedException 토큰에서 사용자 ID를 추출하는 중 오류가 발생한 경우
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
            throw new UnauthorizedException("토큰에서 사용자 ID 복호화 중 예외 발생");
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