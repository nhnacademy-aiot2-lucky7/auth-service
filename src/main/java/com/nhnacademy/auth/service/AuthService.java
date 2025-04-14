package com.nhnacademy.auth.service;


import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;

public interface AuthService {

    String signUp(UserSignUpRequest userSignUpRequest);

    String signIn(UserSignInRequest userSignInRequest);

    void signOut(String accessToken);
}
