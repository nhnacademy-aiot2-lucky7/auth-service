package com.nhnacademy.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@AllArgsConstructor
@EqualsAndHashCode
public class UserResponse {

    @JsonProperty("userNo")
    Long userNo;

    @JsonProperty("userName")
    String userName;

    @JsonProperty("userEmail")
    String userEmail;
}
