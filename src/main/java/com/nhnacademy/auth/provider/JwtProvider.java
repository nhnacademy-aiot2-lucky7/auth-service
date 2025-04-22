package com.nhnacademy.auth.provider;

import com.nhnacademy.auth.util.AESUtil;
import com.nhnacademy.common.exception.UnauthorizedException;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@code JwtProvider} 클래스는 JWT(Json Web Token)의 생성 및 파싱을 담당하는 컴포넌트입니다.
 *
 * <p>이 클래스는 다음 기능들을 제공합니다:</p>
 * <ul>
 *     <li>사용자 ID를 포함한 액세스 토큰 및 리프레시 토큰 생성</li>
 *     <li>JWT 토큰에서 사용자 ID와 만료 시간 추출</li>
 *     <li>JWT 토큰의 남은 유효 시간 계산</li>
 * </ul>
 *
 * <p>토큰 내 사용자 ID는 AES 알고리즘으로 암호화되어 저장되며, 서명 키는 환경 변수 또는 환경 설정 파일을 통해 주입됩니다.</p>
 */

@Slf4j
@Component
public class JwtProvider {
    private final Key key;
    private final AESUtil aesUtil;
    private static final String JWT_SECRET_KEY = "JWT_SECRET";
    // AccessToken 유효시간 = 1시간
    private static final long ACCESS_TOKEN_VALIDITY = 60 * 60 * 1000L;
    // RefreshToken 유효시간 = 일주일
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60  * 60 * 1000L;

    /**
     * {@code JwtProvider} 생성자.
     *
     * @param dotenv 환경변수에서 JWT 비밀키를 로드하는 객체
     * @param env Spring {@link Environment}로부터 비밀키를 보조적으로 로드
     * @param aesUtil 사용자 ID 암호화를 위한 AES 유틸리티 클래스
     * @throws UnauthorizedException JWT 비밀키가 설정되지 않은 경우
     */

    public JwtProvider(Dotenv dotenv, Environment env, AESUtil aesUtil) {
        this.aesUtil = aesUtil;
        String jwtSecretKey = dotenv.get(JWT_SECRET_KEY);

        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            jwtSecretKey = env.getProperty("jwt.secret");
        }

        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            throw new UnauthorizedException("JWT_SECRET가 설정되지 않았습니다.");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 주어진 사용자 ID로 액세스 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 JWT 액세스 토큰 (유효기간: 1시간)
     */

    public String createAccessToken(String userId) {
        return createToken(userId, ACCESS_TOKEN_VALIDITY);
    }

    /**
     * 주어진 사용자 ID로 리프레시 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 JWT 리프레시 토큰 (유효기간: 7일)
     */

    public String createRefreshToken(String userId) {
        return createToken(userId, REFRESH_TOKEN_VALIDITY);
    }

    /**
     * 사용자 ID와 유효 기간을 기반으로 JWT 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param validity 토큰 유효 시간 (밀리초)
     * @return 생성된 JWT 토큰
     * @throws UnauthorizedException 사용자 ID 암호화 실패 시 발생
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
     * JWT 토큰에서 암호화된 사용자 ID를 추출하여 복호화합니다.
     *
     * @param token JWT 토큰
     * @return 복호화된 사용자 ID
     * @throws UnauthorizedException 복호화 실패 또는 파싱 오류 발생 시
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
     * JWT 토큰에서 만료 예정 시간을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 만료 시간 문자열 (형식: yyyy-MM-dd HH:mm:ss)
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
     * JWT 토큰의 남은 유효 시간을 계산합니다.
     *
     * @param token JWT 토큰
     * @return 현재 시점으로부터 토큰 만료까지 남은 시간 (밀리초)
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