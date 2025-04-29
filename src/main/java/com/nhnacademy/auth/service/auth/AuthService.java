package com.nhnacademy.auth.service.auth;

import com.nhnacademy.auth.dto.UserSignInRequest;

public interface AuthService {
    String signIn(UserSignInRequest userSignInRequest);

    void signOut(String accessToken);
}
