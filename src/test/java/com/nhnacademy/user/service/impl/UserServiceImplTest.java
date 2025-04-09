package com.nhnacademy.user.service.impl;

import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserRegisterRequest;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.repository.UserRepository;
import com.nhnacademy.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@Slf4j
@SpringBootTest
class UserServiceImplTest {

    @MockitoBean
    UserRepository userRepository;

    @InjectMocks
    UserService userService;


    @Test
    void createUser() {

        UserRegisterRequest registerUserRequest = new UserRegisterRequest(
                "user1",
                "user1@email.com",
                "user12345?"
        );

        Mockito.when(userRepository.existsByUserEmail(Mockito.anyString())).thenReturn(false);

        UserResponse response = userService.createUser(registerUserRequest);

        Assertions.assertNotNull(response);

        Assertions.assertAll(
                () -> {
                    Assertions.assertEquals("USER", response.getUserRole());
                    Assertions.assertEquals("user1", response.getUserName());
                    Assertions.assertEquals("user1@email.com", response.getUserEmail());

                }
        );
    }

    @Test
    void getUser() {
    }

    @Test
    void loginUser() {

    }
}