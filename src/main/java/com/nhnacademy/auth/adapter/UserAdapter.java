package com.nhnacademy.auth.adapter;

import com.nhnacademy.auth.dto.UserLoginRequest;
import com.nhnacademy.auth.dto.UserRegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name: 호출할 대상 서비스 이름
@FeignClient(name = "user-service", url = "${url.user}", path = "/auth")
public interface UserAdapter {

    @PostMapping("/signUp")
    ResponseEntity<Void> createUser(@RequestBody UserRegisterRequest userRegisterRequest);

    @PostMapping("/signIn")
    ResponseEntity<Void> loginUser(@RequestBody UserLoginRequest userLoginRequest);

}
