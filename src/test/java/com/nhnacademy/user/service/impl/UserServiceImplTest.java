package com.nhnacademy.user.service.impl;

import com.nhnacademy.user.dto.RegisterUserRequest;
import com.nhnacademy.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void registerUser() {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest(

        );
    }

    @Test
    void getUser() {
    }
}