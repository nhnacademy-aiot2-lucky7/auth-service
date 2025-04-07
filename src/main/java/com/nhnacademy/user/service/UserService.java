package com.nhnacademy.user.service;


import com.nhnacademy.user.dto.RegisterUserRequest;
import com.nhnacademy.user.dto.UserResponse;

public interface UserService {
    UserResponse registerUser(RegisterUserRequest loginRequest);
    UserResponse getUser(long userNo);
}
