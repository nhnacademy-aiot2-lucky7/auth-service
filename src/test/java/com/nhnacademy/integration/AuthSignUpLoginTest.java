package com.nhnacademy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.adapter.UserAdapter;
import com.nhnacademy.dto.UserSignUpRequest;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class AuthSignUpLoginTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserAdapter userAdapter;

    @Autowired
    @Qualifier("refreshTokenRedisTemplate")
    RedisTemplate<String, Object> refreshTokenRedisTemplate;

    @Autowired
    @Qualifier("accessTokenBlacklistRedisTemplate")
    RedisTemplate<String, Object> accessTokenBlacklistRedisTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("회원가입 → 로그인 → 로그아웃 성공 흐름")
    void signUpLoginLogoutScenario() throws Exception {
        String email = "test-user@example.com";
        String password = "test1234!";
        String userIdKey = "refreshToken:" + email;

        when(userAdapter.createUser(any())).thenReturn(ResponseEntity.ok("회원가입 완료"));
        when(userAdapter.loginUser(any())).thenReturn(ResponseEntity.ok("user_id"));

        // 1. 회원가입
        mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName": "테스트유저",
                          "userEmail": "%s",
                          "userPassword": "%s",
                          "userPhone": "010-1234-5678",
                          "userDepartment": "개발"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isOk());

        // 2. 로그인 → accessToken 쿠키 추출
        MvcResult result = mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userEmail": "%s",
                          "userPassword": "%s"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andReturn();

        String accessToken = Arrays.stream(result.getResponse().getCookies())
                .filter(c -> "accessToken".equals(c.getName()))
                .findFirst()
                .orElseThrow()
                .getValue();

        // 3. Redis에 refresh token 저장 확인
        assertTrue(refreshTokenRedisTemplate.hasKey(userIdKey), "refreshToken Redis 저장 실패");

        // 4. 로그아웃
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0)); // 쿠키 삭제 확인

        // 5. Redis에서 refresh token 삭제 확인
        assertFalse(refreshTokenRedisTemplate.hasKey(userIdKey), "refreshToken 삭제 실패");

        // 6. access token 블랙리스트 등록 확인
        assertEquals("logout", accessTokenBlacklistRedisTemplate.opsForValue().get("blacklist:" + accessToken));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일로 user-service에서 400 응답")
    void signUpFail_dueToDuplicateEmail() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "중복유저", "dup@example.com", "pass123!", "010-0000-0000", "개발");

        when(userAdapter.createUser(any()))
                .thenReturn(ResponseEntity.status(400).body("이미 존재하는 사용자"));

        mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 사용자"));
    }

    @Test
    @DisplayName("회원가입 실패 - 필드 유효성 검증 실패")
    void signUpFail_dueToInvalidFields() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "", "invalid", "123", "", "");

        mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("요청에 문제가 있습니다")));
    }

    @Test
    @DisplayName("회원가입 실패 - user-service 내부 오류 (FeignException)")
    void signUpFail_dueToUserServiceError() throws Exception {
        UserSignUpRequest request = new UserSignUpRequest(
                "유저", "fail@example.com", "valid1234!", "010-1234-1234", "개발");

        when(userAdapter.createUser(any()))
                .thenThrow(FeignException.InternalServerError.class);

        mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway());
    }
}
