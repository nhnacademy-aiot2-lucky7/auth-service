package com.nhnacademy.auth.adapter;

import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name: 호출할 대상 서비스 이름
@FeignClient(name = "user-service", path = "users")
public interface UserAdapter {

    @PostMapping("/auth/signUp")
    ResponseEntity<String> createUser(@RequestBody UserSignUpRequest userSignUpRequest);

    @PostMapping("/auth/signIn")
    ResponseEntity<String> loginUser(@RequestBody UserSignInRequest userSignInRequest);

}
