package com.nhnacademy.adapter;

import com.nhnacademy.dto.UserSignInRequest;
import com.nhnacademy.dto.UserSignUpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * UserAdapter는 MSA 구조에서 user-service에 HTTP 요청을 위임하는 Feign 클라이언트입니다.
 * <p>
 * 회원가입, 로그인 요청을 user-service의 인증 관련 엔드포인트로 전달합니다.
 */
@FeignClient(name = "user-service", path = "users")
public interface UserAdapter {

    /**
     * 사용자 회원가입 요청을 user-service에 위임합니다.
     *
     * @param userSignUpRequest 회원가입 요청 정보
     * @return 성공 시 2xx, 실패 시 4xx 또는 5xx 응답
     */
    @PostMapping("/auth/signUp")
    ResponseEntity<String> createUser(@RequestBody UserSignUpRequest userSignUpRequest);

    /**
     * 사용자 로그인 요청을 user-service에 위임합니다.
     *
     * @param userSignInRequest 로그인 요청 정보
     * @return 성공 시 200 OK + 사용자 식별자, 실패 시 에러 메시지 포함
     */
    @PostMapping("/auth/signIn")
    ResponseEntity<String> loginUser(@RequestBody UserSignInRequest userSignInRequest);
}
