package com.nhnacademy.controller;

import com.nhnacademy.adapter.UserAdapter;
import com.nhnacademy.dto.UserSignInRequest;
import com.nhnacademy.dto.UserSignUpRequest;
import com.nhnacademy.service.auth.AuthService;
import com.nhnacademy.service.refresh_token.RefreshTokenService;
import com.nhnacademy.common.exception.FailSignUpException;
import com.nhnacademy.token.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 요청을 처리하는 REST 컨트롤러입니다.
 *
 * <p>주요 기능:
 * <ul>
 *     <li>회원가입</li>
 *     <li>로그인 (access token 발급 및 refresh token 저장)</li>
 *     <li>로그아웃 (access token 블랙리스트 등록 및 refresh token 삭제)</li>
 *     <li>access token 재발급</li>
 * </ul>
 * access token은 httpOnly 쿠키로 응답되며, refresh token은 Redis에 저장됩니다.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String STRICT = "Strict";

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final UserAdapter userAdapter;

    /**
     * 사용자 회원가입 요청을 처리합니다. 회원가입 성공 시 자동 로그인 처리도 수행됩니다.
     *
     * @param userSignUpRequest 회원가입 요청 정보
     * @return accessToken을 담은 httpOnly 쿠키
     * @throws FailSignUpException 회원가입 실패 시
     */
    @PostMapping("/signUp")
    public ResponseEntity<Void> signUp(@RequestBody @Validated UserSignUpRequest userSignUpRequest) {
        log.info("[AuthController] 회원가입 요청 - email={}", userSignUpRequest.getUserEmail());

        ResponseEntity<String> response = userAdapter.createUser(userSignUpRequest);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new FailSignUpException(response.getStatusCode().value(), response.getBody());
        }

        log.info("[AuthController] 회원가입 성공 - email={}", userSignUpRequest.getUserEmail());

        return signIn(new UserSignInRequest(userSignUpRequest.getUserEmail(), userSignUpRequest.getUserPassword()));
    }

    /**
     * 사용자 로그인 요청을 처리합니다.
     *
     * @param userSignInRequest 로그인 요청 정보
     * @return accessToken을 담은 httpOnly 쿠키
     */
    @PostMapping("/signIn")
    public ResponseEntity<Void> signIn(@RequestBody @Validated UserSignInRequest userSignInRequest) {
        log.info("[AuthController] 로그인 요청 - email={}", userSignInRequest.getUserEmail());

        String accessToken = authService.signIn(userSignInRequest);
        long ttl = jwtProvider.getRemainingExpiration(accessToken);

        log.info("[AuthController] 로그인 성공 - email={}, TTL(ms)={}", userSignInRequest.getUserEmail(), ttl);

        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(ttl)
                .sameSite(STRICT)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * 사용자 로그아웃 요청을 처리합니다.
     *
     * @param accessToken 쿠키에서 추출한 access token
     * @return 로그아웃 완료 메시지와 쿠키 제거 헤더
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(value = ACCESS_TOKEN, required = false) String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("[AuthController] 로그아웃 요청 실패 - accessToken 쿠키 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("AccessToken이 없습니다.");
        }

        log.info("[AuthController] 로그아웃 요청 - accessToken={}", accessToken);

        authService.signOut(accessToken);

        ResponseCookie expiredCookie = ResponseCookie.from(ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite(STRICT)
                .build();

        log.info("[AuthController] 로그아웃 처리 완료 - accessToken 블랙리스트 등록 및 쿠키 제거");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body("로그아웃 되었습니다.");
    }

    /**
     * access token 재발급 요청을 처리합니다.
     *
     * @param accessToken 기존 access token (쿠키에서 추출)
     * @return 새 access token을 담은 httpOnly 쿠키
     */
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissueToken(@CookieValue(value = ACCESS_TOKEN, required = false) String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("[AuthController] AccessToken 재발급 실패 - 쿠키 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("[AuthController] AccessToken 재발급 요청");

        String newAccessToken = refreshTokenService.reissueAccessToken(accessToken);
        long ttl = jwtProvider.getRemainingExpiration(newAccessToken);

        log.info("[AuthController] AccessToken 재발급 성공 - TTL(ms)={}", ttl);

        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN, newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(ttl)
                .sameSite(STRICT)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
