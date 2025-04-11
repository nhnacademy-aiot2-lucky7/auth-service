package com.nhnacademy.user.service;

import com.nhnacademy.user.dto.UserLoginRequest;
import com.nhnacademy.user.dto.UserRegisterRequest;
import com.nhnacademy.user.dto.UserResponse;

/**
 * 사용자 관련 기능을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 회원 가입, 조회, 로그인, 삭제 등의 기능을 제공합니다.
 */
public interface UserService {

    /**
     * 새로운 사용자를 등록합니다.
     *
     * @param userRegisterRequest 사용자 등록 요청 DTO
     * @return 등록된 사용자 정보 DTO
     */
    UserResponse createUser(UserRegisterRequest userRegisterRequest);

    /**
     * 사용자 번호로 사용자 정보를 조회합니다.
     *
     * @param userEmail 사용자 이메일(아이디)
     * @return 사용자 정보 DTO
     */
    UserResponse getUser(String userEmail);

    /**
     * 로그인 요청 정보로 사용자 정보를 조회합니다.
     *
     * @param userLoginRequest 로그인 요청 DTO
     * @return 사용자 정보 DTO
     */
    UserResponse loginUser(UserLoginRequest userLoginRequest);

    /**
     * 사용자 번호를 기반으로 사용자를 삭제합니다.
     *
     * @param userNo 사용자 번호
     */
    void removeUser(long userNo);
}
