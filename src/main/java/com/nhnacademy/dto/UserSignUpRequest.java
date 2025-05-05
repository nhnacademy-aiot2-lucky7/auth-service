package com.nhnacademy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpRequest {
    @JsonProperty("userName")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해주세요.")
    private String userName;

    @JsonProperty("userEmail")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String userEmail;

    @JsonProperty("userPassword")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{6,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String userPassword;

    @JsonProperty("userPhone")
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Size(max = 20, message = "전화번호는 최대 20자까지 입력할 수 있습니다.")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다. 예: 010-1234-5678")
    private String userPhone;

    @JsonProperty("userDepartment")
    @NotBlank(message = "부서명은 필수 입력 항목입니다.")
    @Size(max = 45, message = "부서명은 최대 45자까지 입력할 수 있습니다.")
    private String userDepartment;
}