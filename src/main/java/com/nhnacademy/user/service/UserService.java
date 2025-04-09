package com.nhnacademy.user.service;


import com.nhnacademy.user.dto.UserLoginRequest;
import com.nhnacademy.user.dto.UserRegisterRequest;
import com.nhnacademy.user.dto.UserResponse;

public interface UserService {
    UserResponse createUser(UserRegisterRequest userRegisterRequest);
    UserResponse getUser(Long userNo);
    UserResponse loginUser(UserLoginRequest userLoginRequest);
    void removeUser(long userNo);
}
