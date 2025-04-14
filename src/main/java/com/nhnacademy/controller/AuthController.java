package com.nhnacademy.controller;

import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
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

    private final int ACCESS_TIME = 60 * 60;    // 1시간 유효

    @PostMapping("/signUp")
    public ResponseEntity<Void> signUp(@RequestBody UserSignUpRequest userSignUpRequest, HttpServletResponse response) {
        String token = authService.signUp(userSignUpRequest);

        // 쿠키에 토큰 담기
        Cookie cookie = new Cookie("ACCESS_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TIME);
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<Void> signIn(@RequestBody UserSignInRequest userSignInRequest, HttpServletResponse response){
        String token = authService.signIn(userSignInRequest);

        // 쿠키에 토큰 담기
        Cookie cookie = new Cookie("ACCESS_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TIME);
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
