package com.nhnacademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.auth.controller.AuthController;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import com.nhnacademy.auth.service.AuthService;
import com.nhnacademy.token.dto.AccessTokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AccessTokenResponse accessTokenResponse;

    @MockitoBean
    AuthService authService;

    @Test
    @DisplayName("회원가입: 201 성공")
    void signUp_201_success() throws Exception {

        UserSignUpRequest request = new UserSignUpRequest("auth", "auth@email.com", "auth12345!");

        Mockito.when(accessTokenResponse.getAccessToken()).thenReturn("accessToken");
        Mockito.when(authService.signUp(Mockito.any(UserSignUpRequest.class))).thenReturn(accessTokenResponse);

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", accessTokenResponse.getAccessToken()));

    }

    @Test
    @DisplayName("로그인: 200 성공")
    void signIn_200_success() throws Exception {
        UserSignInRequest request = new UserSignInRequest("user@email.com", "auth1234!");

        Mockito.when(accessTokenResponse.getAccessToken()).thenReturn("accessToken");
        Mockito.when(authService.signIn(Mockito.any(UserSignInRequest.class))).thenReturn(accessTokenResponse);

        mockMvc.perform(
                post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", accessTokenResponse.getAccessToken()));
    }
}