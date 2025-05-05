package com.nhnacademy.service.auth.impl;

import com.nhnacademy.adapter.UserAdapter;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.dto.UserSignInRequest;
import com.nhnacademy.service.blacklist.BlacklistService;
import com.nhnacademy.service.refresh_token.RefreshTokenService;
import com.nhnacademy.token.provider.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceImplTest {
    @Mock
    UserAdapter userAdapter;

    @Mock
    BlacklistService blacklistService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    @DisplayName("sign in 성공 test")
    void successSignInTest(){
        UserSignInRequest userSignInRequest = new UserSignInRequest("testId", "encrypted password");

        when(userAdapter.loginUser(any(UserSignInRequest.class))).thenReturn(ResponseEntity.ok(userSignInRequest.getUserEmail()));
        when(jwtProvider.createRefreshToken()).thenReturn("refresh_token");
        when(jwtProvider.createAccessToken(userSignInRequest.getUserEmail())).thenReturn("access_token");

        String accessToken = authService.signIn(userSignInRequest);

        assertEquals("access_token", accessToken);
    }

    @Test
    @DisplayName("sign in 실패 test: adaptor return not Status.OK")
    void failedSignInTest(){
        UserSignInRequest userSignInRequest = new UserSignInRequest("testId", "encrypted password");

        when(userAdapter.loginUser(any(UserSignInRequest.class))).thenReturn(ResponseEntity.badRequest().body("뭔가 잘못 됌"));

        assertThrows(FailSignInException.class, ()->authService.signIn(userSignInRequest));
    }

    @Test
    @DisplayName("sign out 성공 test")
    void successSignOutTest(){
        String accessToken = "access_token";

        authService.signOut(accessToken);

        verify(blacklistService, times(1)).addBlacklist(accessToken);
        verify(refreshTokenService, times(1)).removeRefreshToken(accessToken);
    }
}