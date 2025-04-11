package com.nhnacademy.common.controller;

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

@Slf4j
@RestController
@RequestMapping(value = {"/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> createAction(@Validated @RequestBody UserRegisterRequest userRegisterRequest){
        UserResponse userResponse = userService.createUser(userRegisterRequest);

        log.info("create response:{}", userResponse);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @GetMapping(value = {"/{user-no}"})
    public ResponseEntity<UserResponse> getAction(@PathVariable(value = "user-no") Long userNo){
        UserResponse userResponse = userService.getUser(userNo);

        log.info("create response:{}", userResponse);
        return ResponseEntity
                .ok(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginAction(@Validated @RequestBody UserLoginRequest userLoginRequest){
        UserResponse userResponse = userService.loginUser(userLoginRequest);

        log.info("login response:{}", userResponse);

        return ResponseEntity.ok(userResponse);
    }
}
