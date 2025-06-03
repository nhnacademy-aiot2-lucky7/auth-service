package com.nhnacademy.adapter;

import com.nhnacademy.dto.GoogleUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "googleUserInfo", url = "https://www.googleapis.com")
public interface GoogleUserInfoClient {
    @GetMapping(value = "/oauth2/v3/userinfo", consumes = MediaType.APPLICATION_JSON_VALUE)
    GoogleUserInfoResponse getUserInfo(@RequestHeader("Authorization") String authorizationHeader);
}
