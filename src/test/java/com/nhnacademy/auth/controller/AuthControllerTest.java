package com.nhnacademy.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import com.nhnacademy.auth.service.impl.AuthServiceImpl;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.token.provider.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(value = {AuthController.class})
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthServiceImpl authService;

    @MockitoBean
    UserAdapter userAdapter;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    @DisplayName("회원가입: 201 성공")
    void signUp_201_success() throws Exception {

        UserSignUpRequest request = new UserSignUpRequest(
                "auth",
                "auth@email.com",
                "auth12345!",
                "010-1111-0000",
                "개발"
        );

        when(userAdapter.createUser(Mockito.any(UserSignUpRequest.class))).thenReturn(ResponseEntity.ok().build());
        when(authService.signIn(any(UserSignInRequest.class))).thenReturn("accessToken");

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", "accessToken"));
    }

    @Test
    @DisplayName("회원가입 실패: FailSignUpException 발생 확인")
    void signUp_fail_case1() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "auth",
                "auth@email.com",
                "auth12345!",
                "010-1111-0000",
                "개발");

        when(userAdapter.createUser(Mockito.any(UserSignUpRequest.class))).thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("회원가입 실패: email이 null인 경우")
    void signUp_fail_case2() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest("auth",
                null,
                "auth12345!",
                "010-1111-0000",
                "개발");

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("회원가입 실패: name이 null인 경우")
    void signUp_fail_case3() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                null,
                "auth@email.com",
                "auth12345!",
                "01011110000",
                "개발");

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("회원가입 실패: 비밀번호가 null인 경우")
    void signUp_fail_case4() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "auth",
                "auth@email.com",
                null,
                "01011110000",
                "개발");

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("회원가입 실패: email 형식이 틀린 경우")
    void signUp_fail_case5() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "auth",
                "auth",
                "auth12345!",
                "01011110000",
                "개발");

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("회원가입 실패: 이름형식이 틀린 경우")
    void signUp_fail_case6() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "a",
                "auth@email.com",
                "auth12345!",
                "01011110000",
                "개발");

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("회원가입 실패: 비밀번호 형식이 틀린 경우")
    void signUp_fail_case7() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "auth",
                "auth@email.com",
                "auth12",
                "01011110000",
                "개발");

        mockMvc.perform(
                        post("/auth/signUp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인: 200 성공")
    void signIn_200_success() throws Exception {
        UserSignInRequest request = new UserSignInRequest("user@email.com", "auth1234!");

        when(authService.signIn(any(UserSignInRequest.class))).thenReturn("accessToken");

        mockMvc.perform(
                        post("/auth/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", "accessToken"));
    }

    @Test
    @DisplayName("로그인 실패: FailSignInException 발생 확인")
    void signIn_fail_case1() throws Exception {
        UserSignInRequest request = new UserSignInRequest("auth@email.com", "abc1234!!");

        Mockito.when(authService.signIn(Mockito.any(UserSignInRequest.class))).thenThrow(new FailSignInException(404));

        mockMvc.perform(
                        post("/auth/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());

    }

    @Test
    @DisplayName("로그인 실패: email이 null인 경우")
    void signIn_fail_case2() throws Exception {
        UserSignInRequest request = new UserSignInRequest(null, "auth12345!");

        mockMvc.perform(
                        post("/auth/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호가 null인 경우")
    void signIn_fail_case3() throws Exception {
        UserSignInRequest request = new UserSignInRequest("user@email.com", null);

        mockMvc.perform(
                        post("/auth/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 실패: email 형식이 틀린 경우")
    void signIn_fail_case4() throws Exception {
        UserSignInRequest request = new UserSignInRequest("user123", "auth12345!");

        mockMvc.perform(
                        post("/auth/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Bad Request: userEmail - 유효한 이메일 주소를 입력해주세요.; "));
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 형식이 틀린 경우")
    void signIn_fail_case5() throws Exception {
        UserSignInRequest request = new UserSignInRequest("user123@email.com", "auth12");

        mockMvc.perform(
                        post("/auth/signIn")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그아웃 성공 - 쿠키 삭제")
    void logout_success() throws Exception {
        String accessToken = "accessToken";

        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andExpect(content().string("로그아웃 되었습니다."));
    }

    @Test
    @DisplayName("AccessToken 재발급 성공 - 쿠키 반환")
    void refreshAccessToken_success() throws Exception {
        String accessToken = "oldAccessToken";
        String newAccessToken = "newAccessToken";

        when(authService.reissueAccessToken(accessToken)).thenReturn(newAccessToken);

        mockMvc.perform(post("/auth/reissue")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("accessToken=" + newAccessToken)));
    }

}