package com.nhnacademy.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginRequest {

    @NotBlank
    @JsonProperty("userEmail")
    String userEmail;

    @JsonProperty("userPassword")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{6,20}$")
    String userPassword;

    public UserLoginRequest(String userEmail, String userPassword){
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }
}
