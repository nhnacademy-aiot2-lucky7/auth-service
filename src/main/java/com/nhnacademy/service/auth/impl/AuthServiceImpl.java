package com.nhnacademy.service.auth.impl;

import com.nhnacademy.adapter.UserAdapter;
import com.nhnacademy.dto.UserSignInRequest;
import com.nhnacademy.service.auth.AuthService;
import com.nhnacademy.service.blacklist.BlacklistService;
import com.nhnacademy.service.refresh_token.RefreshTokenService;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.token.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 서비스 구현체입니다.
 *
 * <p>회원 로그인 시 accessToken, refreshToken을 발급 및 저장하며,
 * 로그아웃 시 accessToken을 블랙리스트에 등록하고 refreshToken을 삭제합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAdapter userAdapter;
    private final BlacklistService blacklistService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;

    /**
     * 사용자 로그인을 처리합니다.
     * <p>
     * - 유저 인증 어댑터를 통해 로그인
     * - refreshToken 발급 및 Redis 저장
     * - accessToken 발급 후 반환
     *
     * @param userSignInRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @return accessToken 문자열
     * @throws FailSignInException 로그인 실패 시 (예: 401, 403 등)
     */
    @Override
    public String signIn(UserSignInRequest userSignInRequest) {
        ResponseEntity<String> responseEntity = userAdapter.loginUser(userSignInRequest);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String userId = userSignInRequest.getUserEmail();

            String refreshToken = jwtProvider.createRefreshToken();
            refreshTokenService.setRefreshToken(refreshToken, userId);
            log.debug("[AuthService] RefreshToken 저장 - userId={}, token={}", userId, refreshToken);

            String accessToken = jwtProvider.createAccessToken(userId);
            log.debug("[AuthService] AccessToken 생성 - userId={}, token={}", userId, accessToken);

            return accessToken;
        }

        int statusCode = responseEntity.getStatusCode().value();
        log.warn("[AuthService] 로그인 실패 - email={}, status={}", userSignInRequest.getUserEmail(), statusCode);
        throw new FailSignInException(statusCode);
    }

    /**
     * 사용자 로그아웃을 처리합니다.
     * <p>
     * - accessToken 블랙리스트 등록 (Redis)
     * - refreshToken Redis에서 삭제
     *
     * @param accessToken 로그아웃 대상 access token
     */
    @Override
    public void signOut(String accessToken) {
        blacklistService.addBlacklist(accessToken);
        log.debug("[AuthService] AccessToken 블랙리스트 등록 완료");

        refreshTokenService.removeRefreshToken(accessToken);
        log.debug("[AuthService] RefreshToken 삭제 완료");
    }
}
