package com.nhnacademy.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.user.domain.User;
import lombok.*;

@Getter
@NoArgsConstructor
public class UserResponse {

    @JsonProperty("userRole")
    User.Role userRole;

    @JsonProperty("userNo")
    Long userNo;

    @JsonProperty("userName")
    String userName;

    @JsonProperty("userEmail")
    String userEmail;

    public UserResponse(User.Role userRole, Long userNo, String userName, String userEmail){
        this.userRole = userRole;
        this.userNo = userNo;
        this.userName = userName;
        this.userEmail = userEmail;
    }
}
