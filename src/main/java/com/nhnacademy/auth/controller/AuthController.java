package com.nhnacademy.auth.controller;

import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import com.nhnacademy.auth.service.AuthService;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.common.exception.FailSignUpException;
import com.nhnacademy.token.dto.AccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * AuthController 클래스는 인증 관련 API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 이 클래스는 액세스 토큰 재발급 및 로그아웃 기능을 제공하며, 클라이언트의 요청에 따라
 * 새로운 액세스 토큰을 발급하거나, 기존 액세스 토큰을 삭제하여 로그아웃 처리를 합니다.
 * 액세스 토큰은 쿠키에 담아 클라이언트로 전송됩니다.
 * </p>
 */
@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String STRICT = "Strict";

    private final AuthService authService;

    @PostMapping("/signUp")
    public ResponseEntity<Void> signUp(@RequestBody @Validated UserSignUpRequest userSignUpRequest) {

        AccessTokenResponse accessTokenResponse = authService.signUp(userSignUpRequest);

        // 쿠키에 토큰 담기
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN, accessTokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenResponse.getTtl()))
                .sameSite(STRICT)
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<Void> signIn(@RequestBody @Validated UserSignInRequest userSignInRequest){

        AccessTokenResponse accessTokenResponse = authService.signIn(userSignInRequest);

        // 쿠키에 토큰 담기
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN, accessTokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenResponse.getTtl()))
                .sameSite(STRICT)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * 로그아웃 API 엔드포인트입니다.
     * <p>
     * 클라이언트에서 전달한 액세스 토큰을 사용하여 로그아웃 처리를 수행하고,
     * 해당 액세스 토큰을 삭제하여 로그아웃 상태로 만듭니다.
     * </p>
     *
     * @param accessToken 클라이언트에서 전달한 현재 액세스 토큰
     * @return 로그아웃 완료 메시지를 반환하며, 액세스 토큰 쿠키를 삭제합니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = ACCESS_TOKEN) String accessToken) {
        authService.signOut(accessToken);

        // accessToken 쿠키 삭제
        ResponseCookie expiredAccessToken = ResponseCookie.from(ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite(STRICT)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessToken.toString())
                .body("로그아웃 되었습니다.");
    }

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissueToken(@CookieValue(value = ACCESS_TOKEN) String accessToken) {
        AccessTokenResponse accessTokenResponse = authService.reissueAccessToken(accessToken);

        // 쿠키에 토큰 담기
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN, accessTokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenResponse.getTtl()))
                .sameSite(STRICT)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
