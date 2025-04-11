package com.nhnacademy.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 회원가입 요청을 위한 DTO 클래스입니다.
 * <p>
 * 사용자 이름, 이메일, 비밀번호 정보를 포함하며,
 * 유효성 검사를 통해 요청 데이터의 형식을 검증합니다.
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class UserRegisterRequest {

    /**
     * 사용자 이름
     * <p>
     * 2자 이상 20자 이하이며, 공백일 수 없습니다.
     * JSON 필드명: userName
     */
    @JsonProperty("userName")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해주세요.")
    private String userName;

    /**
     * 사용자 이메일
     * <p>
     * 이메일 형식을 따라야 하며, 공백일 수 없습니다.
     * JSON 필드명: userEmail
     */
    @JsonProperty("userEmail")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String userEmail;

    /**
     * 사용자 비밀번호
     * <p>
     * 6자 이상 20자 이하이며, 숫자/영문/특수문자(?포함X)를 각각 하나 이상 포함해야 합니다.
     * JSON 필드명: userPassword
     */
    @JsonProperty("userPassword")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{6,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String userPassword;

    /**
     * 모든 필드를 지정하는 생성자입니다.
     *
     * @param userName     사용자 이름
     * @param userEmail    사용자 이메일
     * @param userPassword 사용자 비밀번호
     */
    public UserRegisterRequest(String userName, String userEmail, String userPassword) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }
}