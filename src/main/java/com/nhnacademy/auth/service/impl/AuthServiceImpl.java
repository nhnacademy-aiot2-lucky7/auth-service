package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserLoginRequest;
import com.nhnacademy.auth.dto.UserRegisterRequest;
import com.nhnacademy.auth.service.AuthService;
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
}
