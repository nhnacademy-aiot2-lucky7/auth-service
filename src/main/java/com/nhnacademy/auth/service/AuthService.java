package com.nhnacademy.auth.service;

import com.nhnacademy.token.dto.AccessTokenResponse;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;

/**
 * AuthService 인터페이스는 인증 관련 서비스 메소드를 정의합니다.
 * <p>
 * 이 인터페이스는 액세스 토큰의 재발급, 로그아웃 시 토큰 삭제 및 블랙리스트 등록,
 * 로그인 시 액세스 토큰과 리프레시 토큰을 생성하는 기능을 제공합니다.
 * </p>
 */
public interface AuthService {

    String signUp(UserSignUpRequest userSignUpRequest);
    /**
     * 사용자 인증 정보를 기반으로 액세스 토큰과 리프레시 토큰을 생성합니다.
     *
     * <p>이 메소드는 로그인 시 사용되며, 사용자 인증 후 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 발급된 리프레시 토큰은 Redis에 저장되며, DB에도 갱신/저장됩니다.</p>
     *
     * @param userId 로그인에 사용할 사용자 ID
     * @return 액세스 토큰과 만료 시간을 포함하는 {@link AccessTokenResponse} 객체
     */
    AccessTokenResponse createAccessAndRefreshToken(String userId);
    String signUp(UserRegisterRequest userSignInRequest);

    String signIn(UserSignInRequest userSignInRequest);

    void signOut(String accessToken);
    /**
     * 액세스 토큰을 재발급하는 메소드입니다.
     *
     * @param accessToken 기존의 액세스 토큰
     * @return 새로 발급된 액세스 토큰과 만료 시간을 포함하는 {@link AccessTokenResponse}
     */
    AccessTokenResponse reissueAccessToken(String accessToken);

    /**
     * 액세스 토큰과 리프레시 토큰을 삭제하고, 블랙리스트에 등록하는 메소드입니다.
     *
     * @param accessToken 로그아웃할 때 사용할 액세스 토큰
     */
    void deleteAccessAndRefreshToken(String accessToken);
    String signIn(UserSignInRequest userSignInRequest);
}
