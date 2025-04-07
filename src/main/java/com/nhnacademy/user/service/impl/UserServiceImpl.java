package com.nhnacademy.user.service.impl;

import com.nhnacademy.common.exception.CommonHttpException;
import com.nhnacademy.common.exception.ConflictException;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.RegisterUserRequest;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.repository.UserRepository;
import com.nhnacademy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse registerUser(RegisterUserRequest registerUserRequest) {

        // 이메일 중복체크
        boolean isExistsEmail = userRepository.existsByUserEmail(registerUserRequest.getUserEmail());
        if(isExistsEmail) {
            throw new ConflictException("Member email [%s] already exists".formatted(registerUserRequest.getUserEmail()));
        }

        User user = User.ofNewUser(
                registerUserRequest.getMbName(),
                registerUserRequest.getUserEmail(),
                registerUserRequest.getUserPassword()
        );
        userRepository.save(user);

        Optional<User> userOptional = userRepository.findById(user.getUserNo());

        if(userOptional.isEmpty()){
            throw new CommonHttpException(404, "user not found");
        }

        return new UserResponse(
                user.getUserNo(),
                user.getUserName(),
                user.getUserEmail()
        );
    }

    @Override
    public UserResponse getUser(long userNo) {

        Optional<User> userOptional = userRepository.findById(userNo);

        if(userOptional.isPresent()){
            User user = userOptional.get();
            return new UserResponse(
                    user.getUserNo(),
                    user.getUserName(),
                    user.getUserEmail()
            );
        }
        throw new CommonHttpException(404, "user not found");
    }
}
