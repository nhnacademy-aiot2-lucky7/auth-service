package com.nhnacademy.user.repository;

import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserResponse;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface CustomUserRepository {
    Optional<UserResponse> findUserResponseByUserNo(Long userNo);
    Optional<UserResponse> findUserResponseByUserEmail(String userEmail);
}
