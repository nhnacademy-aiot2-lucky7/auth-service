package com.nhnacademy.controller;

import com.nhnacademy.auth.dto.UserLoginRequest;
import com.nhnacademy.auth.dto.UserRegisterRequest;
import com.nhnacademy.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signUp")
    public ResponseEntity<Void> signUp(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletResponse response) {
        String token = authService.signUp(userRegisterRequest);

        // 쿠키에 토큰 담기
        Cookie cookie = new Cookie("ACCESS_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간 유효
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<Void> signIn(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response){
        String token = authService.signIn(userLoginRequest);

        // 쿠키에 토큰 담기
        Cookie cookie = new Cookie("ACCESS_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1시간 유효
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
