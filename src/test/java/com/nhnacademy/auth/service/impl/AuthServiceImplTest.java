package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import com.nhnacademy.auth.provider.JwtProvider;
import com.nhnacademy.token.dto.AccessTokenResponse;
import com.nhnacademy.token.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    JwtProvider jwtProvider;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserSignUpRequest userSignUpRequest;
    private UserSignInRequest userSignInRequest;

    @BeforeEach
    void setUp() {

        userSignUpRequest = new UserSignUpRequest("auth", "auth@email.com", "api12345!");
        userSignInRequest = new UserSignInRequest("auth@email.com", "api12345!");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("회원가입: 201 성공")
    void signUp_201_success() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        Mockito.when(userAdapter.createUser(Mockito.any(UserSignUpRequest.class))).thenReturn(responseEntity);
        Mockito.when(jwtProvider.createAccessToken(userSignUpRequest.getUserEmail())).thenReturn("mockToken");

        AccessTokenResponse accessTokenResponse = authService.signUp(userSignUpRequest);
        log.info("accessToken: "+accessTokenResponse.getAccessToken());

        assertNotNull(accessTokenResponse);
        assertEquals("mockToken", accessTokenResponse.getAccessToken());
        verify(userAdapter, times(1)).createUser(userSignUpRequest);
        verify(jwtProvider, times(1)).createAccessToken("auth@email.com");
    }

    @Test
    @DisplayName("회원가입: 400 실패")
    void signUp_400_fail() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(userAdapter.createUser(userSignUpRequest)).thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.signUp(userSignUpRequest));
        assertEquals("SignUp FAIL!!!!", exception.getMessage());
    }

    @Test
    @DisplayName("로그인: 200 성공")
    void signIn_200_success() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(userAdapter.loginUser(userSignInRequest)).thenReturn(responseEntity);
        when(jwtProvider.createAccessToken(userSignInRequest.getUserEmail())).thenReturn("mockToken");

        AccessTokenResponse accessTokenResponse = authService.signIn(userSignInRequest);

        assertNotNull(accessTokenResponse);
        assertEquals("mockToken", accessTokenResponse.getAccessToken());
        verify(userAdapter, times(1)).loginUser(userSignInRequest);
        verify(jwtProvider, times(1)).createAccessToken(userSignInRequest.getUserEmail());
    }

    @Test
    @DisplayName("로그인: 400 실패")
    void signIn_400_fail() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(userAdapter.loginUser(userSignInRequest)).thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.signIn(userSignInRequest));
        assertEquals("SignIn FAIL!!!", exception.getMessage());
    }
}