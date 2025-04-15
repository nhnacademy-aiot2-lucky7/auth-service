package com.nhnacademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserAdapter userAdapter;

    @Test
    @DisplayName("회원가입: 201 성공")
    void signUp_201_success() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest("auth", "auth@email.com", "auth12345!");
        String json = objectMapper.writeValueAsString(request);

        when(userAdapter.createUser(Mockito.any(UserSignUpRequest.class))).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
//        when(jwtProvider.createAccessToken("auth@email.com")).thenReturn("mock-token");

        mockMvc.perform(post("/auth/signUp")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("ACCESS_TOKEN"))
                .andExpect(cookie().value("ACCESS_TOKEN", "mock-token"));
    }

    @Test
    @DisplayName("로그인: 200 성공")
    void signIn_200_success() throws Exception {
        UserSignInRequest request = new UserSignInRequest("user@email.com", "auth1234!");
        String json = objectMapper.writeValueAsString(request);

        when(userAdapter.loginUser(Mockito.any(UserSignInRequest.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
//        when(jwtProvider.createAccessToken("user@email.com")).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/signIn")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("ACCESS_TOKEN"))
                .andExpect(cookie().value("ACCESS_TOKEN", "jwt-token"));
    }
}