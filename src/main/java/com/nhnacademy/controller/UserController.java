package com.nhnacademy.controller;

import com.nhnacademy.user.dto.UserLoginRequest;
import com.nhnacademy.user.dto.UserRegisterRequest;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 요청을 처리하는 REST 컨트롤러입니다.
 * 회원가입, 로그인, 사용자 조회 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping(value = {"/users"})
@RequiredArgsConstructor
public class UserController {

    /**
     * 사용자 서비스 객체입니다. 사용자 생성, 조회, 로그인 등의 로직을 처리합니다.
     */
    private final UserService userService;

    /**
     * 회원가입 요청을 처리합니다.
     *
     * @param userRegisterRequest 회원가입 요청 정보 (이름, 이메일, 비밀번호 등)
     * @return 생성된 사용자 정보를 포함한 응답 (HTTP 201 Created)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> createAction(@Validated @RequestBody UserRegisterRequest userRegisterRequest){
        UserResponse userResponse = userService.createUser(userRegisterRequest);
        log.info("create response:{}", userResponse);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }

    /**
     * 사용자 번호를 기반으로 사용자 정보를 조회합니다.
     *
     * @param encryptedUserEmail 암호화된 사용자 정보
     * @return 사용자 정보 응답 (HTTP 200 OK)
     */
    @GetMapping(value = "/me")
    public ResponseEntity<UserResponse> getAction(@RequestHeader("X-USER-ID") String encryptedUserEmail){

        // 복호화하는 과정 들어가야함

        UserResponse userResponse = userService.getUser(encryptedUserEmail);
        log.info("getAction response:{}", userResponse);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * 로그인 요청을 처리합니다.
     *
     * @param userLoginRequest 로그인 요청 정보 (이메일, 비밀번호 등)
     * @return 로그인된 사용자 정보 응답 (HTTP 200 OK)
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginAction(@Validated @RequestBody UserLoginRequest userLoginRequest){
        UserResponse userResponse = userService.loginUser(userLoginRequest);
        log.info("login response:{}", userResponse);
        return ResponseEntity.ok(userResponse);
    }
}
