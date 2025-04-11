package com.nhnacademy.common.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.controller.UserController;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserLoginRequest;
import com.nhnacademy.user.dto.UserRegisterRequest;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {UserController.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Test
    @DisplayName("회원가입 - 성공")
    void createAction() throws Exception {

        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(
                "user",
                "user@email.com",
                "user12345!"
        );

        UserResponse userResponse = new UserResponse(
                User.Role.USER,
                1l,
                "user",
                "user@email.com"
        );

        Mockito.when(userService.createUser(Mockito.any(UserRegisterRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(userRegisterRequest))
                )
                .andDo(result -> {
                    System.out.println("STATUS: " + result.getResponse().getStatus());
                    System.out.println("BODY: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isCreated());


    }

//    @Test
//    @DisplayName("회원조회")
//    void getAction() throws Exception {
//
//        mockMvc.perform(
//                        get("/users/{user-no}", 1l))
//                .andExpect(status().isOk())
//                .andDo(print());
//    }

    @Test
    @DisplayName("로그인 - 성공")
    void loginAction_success() throws Exception {

        UserLoginRequest loginRequest = new UserLoginRequest(
                "user@email.com",
                "user12345!"
        );

        UserResponse userResponse = new UserResponse(
                User.Role.USER,
                1l,
                "user",
                "user@email.com"
        );

        Mockito.when(userService.loginUser(Mockito.any(UserLoginRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(
                        post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(loginRequest))
                )
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 오류")
    void loginAction_fail1() throws Exception {

        UserLoginRequest request = new UserLoginRequest("", "test1234!");

        mockMvc.perform(
                        post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 오류")
    void loginAction_fail2() throws Exception {

        UserLoginRequest request = new UserLoginRequest("test@email.com", "test1234??");

        mockMvc.perform(
                        post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }
}