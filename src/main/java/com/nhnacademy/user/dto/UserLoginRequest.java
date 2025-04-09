package com.nhnacademy.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginRequest {

    String userEmail;

    String userPassword;
}
