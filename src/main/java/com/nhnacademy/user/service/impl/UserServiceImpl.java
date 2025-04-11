package com.nhnacademy.user.service.impl;

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

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * <p>
 * 회원가입, 조회, 로그인 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * 새로운 사용자를 등록합니다.
     * <p>
     * 이메일 중복 여부를 확인하고, 중복될 경우 {@link ConflictException}을 발생시킵니다.
     *
     * @param registerUserRequest 사용자 등록 요청 DTO
     * @return 등록된 사용자 정보
     * @throws ConflictException 이미 등록된 이메일인 경우
     * @throws NotFoundException 등록 후 사용자 조회 실패 시
     */
    @Override
    public UserResponse createUser(UserRegisterRequest registerUserRequest) {

        log.debug("회원가입 시작! 회원 정보: {}", registerUserRequest);

        boolean isExistsEmail = userRepository.existsByUserEmail(registerUserRequest.getUserEmail());
        if (isExistsEmail) {
            throw new ConflictException("이미 존재하는 이메일입니다. 이메일: " + registerUserRequest.getUserEmail());
        }

        User user = User.ofNewUser(
                registerUserRequest.getUserName(),
                registerUserRequest.getUserEmail(),
                registerUserRequest.getUserPassword()
        );
        userRepository.save(user);

        Optional<UserResponse> userResponseOptional = userRepository.findUserResponseByUserNo(user.getUserNo());

        if (userResponseOptional.isEmpty()) {
            throw new NotFoundException("유저 정보를 찾을 수 없습니다. userNo: " + user.getUserNo());
        }

        return userResponseOptional.get();
    }

    /**
     * 사용자 번호로 사용자 정보를 조회합니다.
     *
     * @param userEmail 사용자 이메일
     * @return 사용자 응답 DTO
     * @throws NotFoundException 사용자 정보가 없을 경우
     */
    @Override
    public UserResponse getUser(String userEmail) {
        log.debug("회원조회 시작! 회원 이메일 : {}", userEmail);
        return userRepository.findUserResponseByUserEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("해당 userEmail에 해당하는 유저를 찾을 수 없습니다."));
    }

    /**
     * 로그인 시 이메일을 통해 사용자 정보를 조회합니다.
     *
     * @param userLoginRequest 로그인 요청 DTO
     * @return 사용자 응답 DTO
     * @throws NotFoundException 이메일에 해당하는 사용자가 없을 경우
     */
    @Override
    public UserResponse loginUser(UserLoginRequest userLoginRequest) {
        log.debug("로그인 시작! 회원 이메일: {}", userLoginRequest.getUserEmail());
        return userRepository.findUserResponseByUserEmail(userLoginRequest.getUserEmail())
                .orElseThrow(() -> new NotFoundException("해당 userEmail에 해당하는 유저를 찾을 수 없습니다."));
    }

    /**
     * 사용자를 삭제합니다.
     * (구현 예정)
     *
     * @param userNo 사용자 번호
     */
    @Override
    public void removeUser(long userNo) {
        log.debug("회원삭제 시작! 회원 번호: {}", userNo);
    }
}