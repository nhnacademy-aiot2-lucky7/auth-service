package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import com.nhnacademy.auth.service.AuthService;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.common.exception.FailSignUpException;
import com.nhnacademy.common.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAdapter userAdapter;
    private final JwtProvider jwtProvider;

    @Override
    public String signUp(UserSignUpRequest userSignUpRequest) {
        ResponseEntity<Void> responseEntity = userAdapter.createUser(userSignUpRequest);

        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            // 회원가입 성공 -> 토큰 생성
            return jwtProvider.createAccessToken(userSignUpRequest.getUserEmail());
        } else {
            throw new FailSignUpException(responseEntity.getStatusCode().value());
        }
    }

    @Override
    public String signIn(UserSignInRequest userSignInRequest) {
        ResponseEntity<Void> responseEntity = userAdapter.loginUser(userSignInRequest);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // 로그인 성공 -> 토큰 생성
            return jwtProvider.createAccessToken(userSignInRequest.getUserEmail());
        } else {
            throw new FailSignInException(responseEntity.getStatusCode().value());
        }
    }

    @Override
    public void signOut(String accessToken) {

    }
}
