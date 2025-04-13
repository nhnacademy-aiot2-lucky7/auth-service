package com.nhnacademy.auth.controller;

import com.nhnacademy.auth.service.AuthService;
import com.nhnacademy.auth.service.impl.AuthServiceImpl;
import com.nhnacademy.token.dto.AccessTokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.Cookie;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(value = {AuthController.class})
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    @Test
    @DisplayName("AccessToken 재발급 성공 - 쿠키 반환")
    void refreshAccessToken_success() throws Exception {
        String accessToken = "oldAccessToken";
        String newAccessToken = "newAccessToken";

        AccessTokenResponse response = new AccessTokenResponse(newAccessToken, 60 * 60 * 1000L);

        when(authService.reissueAccessToken(accessToken)).thenReturn(response);

        mockMvc.perform(post("/api/auth/token/refresh")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("accessToken=" + newAccessToken)))
                .andExpect(content().string("AccessToken 재발급 성공!"));
    }

    @Test
    @DisplayName("로그아웃 성공 - 쿠키 삭제")
    void logout_success() throws Exception {
        String accessToken = "AccessToken";

        doNothing().when(authService).deleteAccessAndRefreshToken(accessToken);

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andExpect(content().string("로그아웃 되었습니다."));
    }
}