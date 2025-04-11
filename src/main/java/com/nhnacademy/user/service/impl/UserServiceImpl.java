package com.nhnacademy.user.service.impl;

import com.nhnacademy.common.exception.CommonHttpException;
import com.nhnacademy.common.exception.ConflictException;
import com.nhnacademy.common.exception.NotFoundException;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserLoginRequest;
import com.nhnacademy.user.dto.UserRegisterRequest;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.repository.UserRepository;
import com.nhnacademy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(UserRegisterRequest registerUserRequest) {

        log.debug("회원가입 시작! 회원 정보: {}", registerUserRequest);
        // 이메일 중복체크
        boolean isExistsEmail = userRepository.existsByUserEmail(registerUserRequest.getUserEmail());
        if (isExistsEmail) {
            throw new ConflictException("이미 존재하는 이메일입니다. 이메일: "+registerUserRequest.getUserEmail());
        }

        User user = User.ofNewUser(
                registerUserRequest.getUserName(),
                registerUserRequest.getUserEmail(),
                registerUserRequest.getUserPassword()
        );
        userRepository.save(user);

        Optional<UserResponse> userResponseOptional = userRepository.findUserResponseByUserNo(user.getUserNo());

        if(userResponseOptional.isEmpty()){
            throw new NotFoundException("유저 정보를 찾을 수 없습니다. userNo: "+ user.getUserNo());
        }

        return userResponseOptional.get();
    }

    @Override
    public UserResponse getUser(Long userNo) {

        log.debug("회원조회 시작! 회원 번호 : {}", userNo);
        return userRepository.findUserResponseByUserNo(userNo)
                .orElseThrow(() -> new NotFoundException("해당 userNo에 해당하는 유저를 찾을 수 없습니다."));
    }

    @Override
    public UserResponse loginUser(UserLoginRequest userLoginRequest) {

        log.debug("로그인 시작! 회원 이메일: {}", userLoginRequest.getUserEmail());
        return userRepository.findUserResponseByUserEmail(userLoginRequest.getUserEmail())
                .orElseThrow(() -> new NotFoundException("해당 userEmail에 해당하는 유저를 찾을 수 없습니다."));
    }

    @Override
    public void removeUser(long userNo) {

        log.debug("회원삭제 시작! 회원 번호: {}", userNo);
    }
}
