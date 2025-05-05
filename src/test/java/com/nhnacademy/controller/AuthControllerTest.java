package com.nhnacademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.adapter.UserAdapter;
import com.nhnacademy.common.advice.CommonAdvice;
import com.nhnacademy.dto.UserSignInRequest;
import com.nhnacademy.dto.UserSignUpRequest;
import com.nhnacademy.service.auth.AuthService;
import com.nhnacademy.service.refresh_token.RefreshTokenService;
import com.nhnacademy.token.provider.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(CommonAdvice.class)
class AuthControllerTest {
    @MockitoBean
    AuthService authService;

    @MockitoBean
    RefreshTokenService refreshTokenService;

    @MockitoBean
    JwtProvider jwtProvider;

    @MockitoBean
    UserAdapter userAdapter;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 + 자동 로그인 쿠키 반환")
    void signUpTest() throws Exception{
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest(
                "testName",
                "test@test.com",
                "testtest1!",
                "010-1111-0000",
                "개발"
        );
        String accessToken = "access_token";
        long ttl = 3600000L;

        when(userAdapter.createUser(any(UserSignUpRequest.class)))
                .thenReturn(ResponseEntity.ok("회원가입 성공"));

        when(authService.signIn(any(UserSignInRequest.class)))
                .thenReturn(accessToken);

        when(jwtProvider.getRemainingExpiration(accessToken))
                .thenReturn(ttl);

        String json = objectMapper.writeValueAsString(userSignUpRequest);

        mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("accessToken=" + accessToken)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Max-Age=" + (ttl / 1000))));
    }

    @Test
    @DisplayName("회원가입 실패 시 예외 발생")
    void signUp_failure() throws Exception {
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest(
                "testName",
                "test@test.com",
                "testtest1!",
                "010-1111-0000",
                "개발"
        );
        String json = objectMapper.writeValueAsString(userSignUpRequest);

        when(userAdapter.createUser(any(UserSignUpRequest.class)))
                .thenReturn(ResponseEntity.status(400).body("이미 존재하는 사용자"));

        mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 성공 + 자동 쿠키 반환")
    void signInTest() throws Exception{
        UserSignInRequest userSignInRequest = new UserSignInRequest(
                "test@test.com",
                "testtest1!"
        );
        String accessToken = "access_token";
        long ttl = 3600000L;

        when(authService.signIn(any(UserSignInRequest.class)))
                .thenReturn(accessToken);

        when(jwtProvider.getRemainingExpiration(accessToken))
                .thenReturn(ttl);

        String json = objectMapper.writeValueAsString(userSignInRequest);

        mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("accessToken=" + accessToken)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Max-Age=" + (ttl / 1000))));
    }

    @Test
    @DisplayName("로그아웃 성공 + 쿠키 삭제")
    void signOutTest() throws Exception{
        String accessToken = "access_token";

        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 되었습니다."))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("accessToken=;")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Max-Age=0")));

        // 서비스 호출 검증 (선택)
        verify(authService, times(1)).signOut(accessToken);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - accessToken 없음")
    void logout_noAccessToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("AccessToken이 없습니다."));
    }

    @Test
    @DisplayName("AccessToken 재발급 성공")
    void reissue_success() throws Exception {
        String oldAccessToken = "old_token";
        String newAccessToken = "new_token";
        long ttl = 3600000L;

        when(refreshTokenService.reissueAccessToken(oldAccessToken)).thenReturn(newAccessToken);
        when(jwtProvider.getRemainingExpiration(newAccessToken)).thenReturn(ttl);

        mockMvc.perform(post("/auth/reissue")
                        .cookie(new Cookie("accessToken", oldAccessToken)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("accessToken=" + newAccessToken)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("Max-Age=" + ttl)));
    }

    @Test
    @DisplayName("AccessToken 재발급 실패 - 쿠키 없음")
    void reissue_fail_noToken() throws Exception {
        mockMvc.perform(post("/auth/reissue"))
                .andExpect(status().isUnauthorized());
    }
}