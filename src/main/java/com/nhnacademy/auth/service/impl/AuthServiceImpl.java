package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.provider.JwtProvider;
import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserLoginRequest;
import com.nhnacademy.auth.dto.UserRegisterRequest;
import com.nhnacademy.auth.service.AuthService;
import com.nhnacademy.common.exception.NotFoundException;
import com.nhnacademy.common.exception.UnauthorizedException;
import com.nhnacademy.token.domain.RefreshToken;
import com.nhnacademy.token.dto.AccessTokenResponse;
import com.nhnacademy.token.repository.RefreshTokenRepository;
import com.nhnacademy.common.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * AuthServiceImpl 클래스는 인증 관련 비즈니스 로직을 구현한 서비스 클래스입니다.
 * <p>
 * 이 서비스는 JWT 토큰을 관리하고, 액세스 토큰을 재발급하거나 로그아웃 시 토큰을 블랙리스트에 등록하는 기능을 제공합니다.
 * </p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String LOGOUT_VALUE = "logout";
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    private final UserAdapter userAdapter;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;


    @Override
    public String signUp(UserRegisterRequest userRegisterRequest) {
        ResponseEntity<Void> responseEntity = userAdapter.createUser(userRegisterRequest);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            // 회원가입 성공 -> 토큰 생성
            String token = jwtProvider.createAccessToken(userRegisterRequest.getUserEmail());
            return token;
        } else {
            throw new RuntimeException("회원가입 실패: 상태코드 " + responseEntity.getStatusCode());
        }
    }

    @Override
    public String signIn(UserLoginRequest userLoginRequest) {
        ResponseEntity<Void> responseEntity = userAdapter.loginUser(userLoginRequest);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // 로그인 성공 -> 토큰 생성
            String token = jwtProvider.createAccessToken(userLoginRequest.getUserEmail());
            return token;
        } else {
            throw new RuntimeException("로그인 실패: 상태코드 " + responseEntity.getStatusCode());
        }
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 생성하는 메소드입니다.
     * <p>
     * 이 메소드는 주어진 사용자 ID에 대해 새로 발급된 액세스 토큰과 리프레시 토큰을 반환합니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @return 새로 생성된 액세스 토큰과 만료 시간을 포함하는 {@link AccessTokenResponse}
     * @throws UnauthorizedException 유효하지 않은 userId인 경우 예외 발생
     */
    @Override
    public AccessTokenResponse createAccessAndRefreshToken(String userId) {
        if (Objects.isNull(userId)) {
            throw new UnauthorizedException("유효하지 않은 AccessToken입니다.");
        }

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);
        // Redis에 refreshToken 저장 (유효기간 설정)
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, refreshToken, 7, TimeUnit.DAYS);  // 예: 7일간 유효

        // DB에 refreshToken 저장
        RefreshToken existingRefreshToken = refreshTokenRepository.findById(userId).orElse(null);
        if (existingRefreshToken != null) {
            // 기존 refreshToken이 있으면 갱신
            existingRefreshToken.setToken(refreshToken);
            refreshTokenRepository.save(existingRefreshToken);
            // 새로운 refreshToken 저장
            RefreshToken newRefreshToken = new RefreshToken(userId, refreshToken);
            refreshTokenRepository.save(newRefreshToken);

            // 생성된 토큰을 반환
            return new AccessTokenResponse(accessToken, jwtProvider.getRemainingExpiration(accessToken));
        }
    }

    /**
     * 액세스 토큰을 재발급하는 메소드입니다.
     *
     * @param accessToken 재발급할 액세스 토큰
     * @return 재발급된 액세스 토큰과 그 만료 시간을 포함하는 {@link AccessTokenResponse}
     * @throws UnauthorizedException 액세스 토큰이 유효하지 않거나 저장된 리프레시 토큰을 찾을 수 없는 경우
     */
    @Override
    public AccessTokenResponse reissueAccessToken(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);
        if (Objects.isNull(userId)) {
            throw new UnauthorizedException("유효하지 않은 AccessToken입니다.");
        }

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(userId);
        if (optionalRefreshToken.isEmpty()) {
            throw new UnauthorizedException("저장된 RefreshToken을 찾을 수 없습니다.");
        }

        String refreshToken = optionalRefreshToken.get().getToken();
        if (!refreshToken.equals(accessToken)) {
            throw new UnauthorizedException("저장된 refreshToken과 다릅니다.");
        }

        // AccessToken 재발급
        String newAccessToken = jwtProvider.createAccessToken(userId);

        // 만료 예정 AccessToken의 ttl
        long expiredAccessTokenTTL = jwtProvider.getRemainingExpiration(accessToken);
        // 만료 예정 AccessToken 블랙리스트 등록
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, LOGOUT_VALUE, expiredAccessTokenTTL, TimeUnit.MILLISECONDS);

        return new AccessTokenResponse(newAccessToken, jwtProvider.getRemainingExpiration(newAccessToken));
    }

    /**
     * 사용자가 로그아웃할 때, 액세스 토큰과 리프레시 토큰을 블랙리스트에 등록하고 DB에서 리프레시 토큰을 삭제하는 메소드입니다.
     *
     * @param accessToken 로그아웃할 때 사용할 액세스 토큰
     * @throws NotFoundException DB에서 리프레시 토큰을 찾을 수 없는 경우
     */
    @Override
    public void deleteAccessAndRefreshToken(String accessToken) {
        String userId = jwtProvider.getUserIdFromToken(accessToken);

        // AccessToken 블랙리스트 등록
        long expiredAccessTokenTtl = jwtProvider.getRemainingExpiration(accessToken);
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, LOGOUT_VALUE, expiredAccessTokenTtl, TimeUnit.MILLISECONDS);

        // RefreshToken 꺼내서 블랙리스트 등록
        RefreshToken refreshToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 아이디의 RefreshToken을 찾을 수 없습니다. userId: " + userId));
        long refreshTokenTtl = jwtProvider.getRemainingExpiration(refreshToken.getToken());
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + refreshToken.getToken(), LOGOUT_VALUE, refreshTokenTtl, TimeUnit.MILLISECONDS);

        // DB에서 RefreshToken 제거
        refreshTokenRepository.delete(refreshToken);
    }
}
