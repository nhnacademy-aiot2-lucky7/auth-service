package com.nhnacademy.service.auth;

import com.nhnacademy.dto.UserSignInRequest;

public interface AuthService {
    String signIn(UserSignInRequest userSignInRequest);

    void signOut(String accessToken);
}
