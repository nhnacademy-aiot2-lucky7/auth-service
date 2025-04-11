package com.nhnacademy.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 로그인 요청을 위한 DTO 클래스입니다.
 * <p>
 * 이메일과 비밀번호를 받아 로그인 검증에 사용됩니다.
 */
@Getter
@NoArgsConstructor
public class UserLoginRequest {

    /**
     * 사용자 이메일
     * <p>
     * 공백이 아닌 문자열이어야 하며, JSON 필드 이름은 "userEmail"입니다.
     */
    @NotBlank
    @JsonProperty("userEmail")
    String userEmail;

    /**
     * 사용자 비밀번호
     * <p>
     * 최소 6자 이상 20자 이하이며, 숫자/영문자/특수문자를 각각 최소 하나 포함해야 합니다.
     * JSON 필드 이름은 "userPassword"입니다.
     */
    @JsonProperty("userPassword")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{6,20}$")
    String userPassword;

    /**
     * 생성자 - 이메일과 비밀번호를 지정하여 객체를 생성합니다.
     *
     * @param userEmail    사용자 이메일
     * @param userPassword 사용자 비밀번호
     */
    public UserLoginRequest(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }
}