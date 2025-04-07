package com.nhnacademy.user.service.impl;

import com.nhnacademy.common.exception.ConflictException;
import com.nhnacademy.user.dto.RegisterUserRequest;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.repository.UserRepository;
import com.nhnacademy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse registerUser(RegisterUserRequest registerUserRequest) {

        //1.이메일 중복체크
        boolean isExistsEmail = userRepository.existsByUserEmail(registerUserRequest.getUserEmail());
        if(isExistsEmail) {
            throw new ConflictException("Member email [%s] already exists".formatted(registerUserRequest.getUserEmail()));
        }


        return null;
    }

    @Override
    public UserResponse getUser(long userNo) {
        return null;
    }
}
