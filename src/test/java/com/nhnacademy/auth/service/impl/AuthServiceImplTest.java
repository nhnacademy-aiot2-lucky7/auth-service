package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserLoginRequest;
import com.nhnacademy.auth.dto.UserRegisterRequest;
import com.nhnacademy.common.provider.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@Slf4j
@ExtendWith(SpringExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAdapter userAdapter;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegisterRequest userRegisterRequest;
    private UserLoginRequest userLoginRequest;

    @BeforeEach
    void setUp() {

        userRegisterRequest = new UserRegisterRequest("auth", "auth@email.com", "api12345!");
        userLoginRequest = new UserLoginRequest("auth@email.com", "api12345!");
    }

    @Test
    @DisplayName("회원가입: 201 성공")
    void signUp_201_success() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);
        when(userAdapter.createUser(userRegisterRequest)).thenReturn(responseEntity);
        when(jwtProvider.createAccessToken(userRegisterRequest.getUserEmail())).thenReturn("mockToken");

        String token = authService.signUp(userRegisterRequest);

        assertNotNull(token);
        assertEquals("mockToken", token);
        verify(userAdapter, times(1)).createUser(userRegisterRequest);
        verify(jwtProvider, times(1)).createAccessToken(userRegisterRequest.getUserEmail());
    }

    @Test
    @DisplayName("회원가입: 400 실패")
    void signUp_400_fail() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(userAdapter.createUser(userRegisterRequest)).thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.signUp(userRegisterRequest));
        assertEquals("회원가입 실패: 상태코드 400 BAD_REQUEST", exception.getMessage());
    }

    @Test
    @DisplayName("로그인: 200 성공")
    void signIn_200_success() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(userAdapter.loginUser(userLoginRequest)).thenReturn(responseEntity);
        when(jwtProvider.createAccessToken(userLoginRequest.getUserEmail())).thenReturn("mockToken");

        String token = authService.signIn(userLoginRequest);

        assertNotNull(token);
        assertEquals("mockToken", token);
        verify(userAdapter, times(1)).loginUser(userLoginRequest);
        verify(jwtProvider, times(1)).createAccessToken(userLoginRequest.getUserEmail());
    }

    @Test
    @DisplayName("로그인: 400 실패")
    void signIn_400_fail() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(userAdapter.loginUser(userLoginRequest)).thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.signIn(userLoginRequest));
        assertEquals("로그인 실패: 상태코드 400 BAD_REQUEST", exception.getMessage());
    }
}