package com.nhnacademy.service.auth;

import com.nhnacademy.dto.UserSignInRequest;

public interface AuthService {
    String signIn(UserSignInRequest userSignInRequest);

    String socialSignIn(String userEmail);

    void signOut(String accessToken);
}
