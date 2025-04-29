package com.nhnacademy.auth.service.auth.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.service.auth.AuthService;
import com.nhnacademy.auth.service.blacklist.BlacklistService;
import com.nhnacademy.auth.service.refresh_token.RefreshTokenService;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.token.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserAdapter userAdapter;
    private final BlacklistService blacklistService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;


    @Override
    public String signIn(UserSignInRequest userSignInRequest) {
        ResponseEntity<String> responseEntity = userAdapter.loginUser(userSignInRequest);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // 로그인 성공 -> 토큰 생성
            String userId = userSignInRequest.getUserEmail();
            String refreshToken = jwtProvider.createRefreshToken();

            refreshTokenService.setRefreshToken(refreshToken, userId);

            return jwtProvider.createAccessToken(userId);
        } else {
            throw new FailSignInException(responseEntity.getStatusCode().value());
        }
    }

    @Override
    public void signOut(String accessToken) {
        // AccessToken 블랙리스트 등록
        blacklistService.addBlacklist(accessToken);
        // refresh token 삭제
        refreshTokenService.removeRefreshToken(accessToken);
    }
}
