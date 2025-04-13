package com.nhnacademy.auth.service;


import com.nhnacademy.auth.dto.UserLoginRequest;
import com.nhnacademy.auth.dto.UserRegisterRequest;

public interface AuthService {

    String signUp(UserRegisterRequest userRegisterRequest);

    String signIn(UserLoginRequest userLoginRequest);
}
