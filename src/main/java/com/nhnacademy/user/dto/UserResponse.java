package com.nhnacademy.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 클라이언트에게 반환할 때 사용하는 응답 DTO입니다.
 * <p>
 * 사용자 번호, 이름, 이메일, 권한(role)을 포함합니다.
 */
@Getter
@NoArgsConstructor
public class UserResponse {

    /**
     * 사용자 권한 (예: USER, ADMIN)
     * <p>
     * JSON 필드명: userRole
     */
    @JsonProperty("userRole")
    User.Role userRole;

    /**
     * 사용자 고유 번호
     * <p>
     * JSON 필드명: userNo
     */
    @JsonProperty("userNo")
    Long userNo;

    /**
     * 사용자 이름
     * <p>
     * JSON 필드명: userName
     */
    @JsonProperty("userName")
    String userName;

    /**
     * 사용자 이메일
     * <p>
     * JSON 필드명: userEmail
     */
    @JsonProperty("userEmail")
    String userEmail;

    /**
     * 모든 사용자 정보를 포함하는 생성자입니다.
     *
     * @param userRole  사용자 권한
     * @param userNo    사용자 번호
     * @param userName  사용자 이름
     * @param userEmail 사용자 이메일
     */
    public UserResponse(User.Role userRole, Long userNo, String userName, String userEmail) {
        this.userRole = userRole;
        this.userNo = userNo;
        this.userName = userName;
        this.userEmail = userEmail;
    }
}