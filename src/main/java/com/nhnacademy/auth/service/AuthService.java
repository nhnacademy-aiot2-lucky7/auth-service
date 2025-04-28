package com.nhnacademy.auth.service;

import com.nhnacademy.auth.dto.UserSignInRequest;

public interface AuthService {
    String signIn(UserSignInRequest userSignInRequest);

    void signOut(String accessToken);

    String reissueAccessToken(String accessToken);
}
