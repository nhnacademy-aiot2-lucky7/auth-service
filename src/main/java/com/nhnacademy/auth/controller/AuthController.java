package com.nhnacademy.auth.controller;

import com.nhnacademy.auth.service.AuthService;
import com.nhnacademy.token.dto.AccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * AuthController 클래스는 인증 관련 API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 이 클래스는 액세스 토큰 재발급 및 로그아웃 기능을 제공하며, 요청에 따라
 * 새로운 액세스 토큰을 발급하거나, 기존 액세스 토큰을 삭제하여 로그아웃 처리합니다.
 * </p>
 */
@RestController
@RequestMapping(value = "/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 액세스 토큰을 재발급하는 API 엔드포인트입니다.
     * <p>
     * 클라이언트에서 전달한 액세스 토큰을 사용하여 새로운 액세스 토큰을 재발급하고,
     * 재발급된 액세스 토큰을 쿠키에 설정하여 응답합니다.
     * </p>
     *
     * @param accessToken 클라이언트에서 전달한 현재 액세스 토큰
     * @return 새로 발급된 액세스 토큰을 포함한 쿠키와 함께 성공 메시지를 반환
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue(value = "accessToken") String accessToken) {
        AccessTokenResponse accessTokenResponse = authService.reissueAccessToken(accessToken);

        ResponseCookie cookie = ResponseCookie.from("accessToken", accessTokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenResponse.getTtl()))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("AccessToken 재발급 성공!");
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
    public ResponseEntity<?> logout(@CookieValue(value = "accessToken") String accessToken) {
        authService.deleteAccessAndRefreshToken(accessToken);

        // accessToken 쿠키 삭제
        ResponseCookie expiredAccessToken = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessToken.toString())
                .body("로그아웃 되었습니다.");
    }
}
