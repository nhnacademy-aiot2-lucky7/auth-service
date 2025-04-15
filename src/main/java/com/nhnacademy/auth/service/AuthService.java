package com.nhnacademy.auth.service;

import com.nhnacademy.token.dto.AccessTokenResponse;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;

public interface AuthService {

    String signUp(UserSignUpRequest userSignInRequest);
    String signIn(UserSignInRequest userSignInRequest);
    void signOut(String accessToken);
    AccessTokenResponse reissueAccessToken(String accessToken);
    AccessTokenResponse createAccessAndRefreshToken(String userId);
    void deleteAccessAndRefreshToken(String accessToken);
}
