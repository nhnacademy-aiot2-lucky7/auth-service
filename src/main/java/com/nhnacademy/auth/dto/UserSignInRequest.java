package com.nhnacademy.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 로그인 요청을 위한 DTO 클래스입니다.
 * <p>
 * 이메일과 비밀번호를 받아 로그인 검증에 사용됩니다.
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class UserSignInRequest {

    /**
     * 사용자 이메일
     * <p>
     * 공백이 아닌 문자열이어야 하며, JSON 필드 이름은 "userEmail"입니다.
     */
    @JsonProperty("userEmail")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    String userEmail;

    /**
     * 사용자 비밀번호
     * <p>
     * 최소 6자 이상 20자 이하이며, 숫자/영문자/특수문자를 각각 최소 하나 포함해야 합니다.
     * JSON 필드 이름은 "userPassword"입니다.
     */
    @JsonProperty("userPassword")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{6,20}",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    String userPassword;

    public UserSignInRequest(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }
}