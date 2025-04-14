package com.nhnacademy.user.service.impl;

import com.nhnacademy.common.exception.NotFoundException;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserLoginRequest;
import com.nhnacademy.user.dto.UserRegisterRequest;
import com.nhnacademy.user.dto.UserResponse;
import com.nhnacademy.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Comment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.Optional;


@Slf4j
@ExtendWith(SpringExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.ofNewUser(
                "user1",
                "user1@email.com",
                "user12345?"
        );
    }

    @Test
    @Comment("회원가입: user 등록")
    void createUser() {

        UserRegisterRequest registerUserRequest = new UserRegisterRequest(
                testUser.getUserName(),
                testUser.getUserEmail(),
                testUser.getUserPassword()
        );

        UserResponse fakeResponse = new UserResponse(
                User.Role.USER, 1l, "user1", "user1@email.com"
        );


        Mockito.when(userRepository.existsByUserEmail(Mockito.anyString())).thenReturn(false);
        Mockito.when(userRepository.findUserResponseByUserNo(Mockito.anyLong()))
                .thenReturn(Optional.of(fakeResponse));
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));

        // userRepository.save를 호출하면 userNo가 생성됩니다.
        Mockito.doAnswer(invocation -> {
            User paramUser = invocation.getArgument(0);
            Field field = User.class.getDeclaredField("userNo");
            field.setAccessible(true);
            field.set(paramUser, 1L);
            log.debug("paramUser: {}", paramUser);
            return null;
        }).when(userRepository).save(Mockito.any(User.class));

        UserResponse response = userService.createUser(registerUserRequest);

        Assertions.assertNotNull(response);

        Assertions.assertAll(
                () -> {
                    Assertions.assertEquals(User.Role.USER, response.getUserRole());
                    Assertions.assertEquals("user1", response.getUserName());
                    Assertions.assertEquals("user1@email.com", response.getUserEmail());
                }
        );
    }

    @Test
    @Comment("user조회: 성공")
    void getUser_success() {
        UserResponse fakeResponse = new UserResponse(
                User.Role.USER, 1l, "user1", "user1@email.com"
        );
        Mockito.when(userRepository.findUserResponseByUserNo(Mockito.anyLong())).thenReturn(Optional.of(fakeResponse));

        UserResponse userResponse = userService.getUser(1l);

        Mockito.verify(userRepository, Mockito.times(1)).findUserResponseByUserNo(Mockito.anyLong());
        Assertions.assertAll(
                () -> {
                    Assertions.assertNotNull(userResponse.getUserNo());
                    Assertions.assertEquals(User.Role.USER, userResponse.getUserRole());
                    Assertions.assertEquals("user1", userResponse.getUserName());
                    Assertions.assertEquals("user1@email.com", userResponse.getUserEmail());
                }
        );

    }

    @Test
    @Comment("user조회: 실패")
    void getUser_fail() {

        // 빈 Optional을 주면 에러 발생
        Mockito.when(userRepository.findUserResponseByUserNo(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        // 존재하지 않는 userNo 호출시 에러 발생
        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.getUser(999L);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findUserResponseByUserNo(Mockito.anyLong());
    }

    @Test
    @Comment("로그인_성공")
    void loginUser_success() {

        UserResponse fakeResponse = new UserResponse(
                User.Role.USER, 1l, "user1", "user1@email.com"
        );
        UserLoginRequest loginRequest = new UserLoginRequest(
                "user1@email.com",
                "user12345?"
        );

        Mockito.when(userRepository.findUserResponseByUserEmail(Mockito.anyString())).thenReturn(Optional.of(fakeResponse));
        UserResponse userResponse = userService.loginUser(loginRequest);

        Mockito.verify(userRepository, Mockito.times(1)).findUserResponseByUserEmail(Mockito.anyString());
        Assertions.assertAll(
                () -> {
                    Assertions.assertNotNull(userResponse.getUserNo());
                    Assertions.assertEquals(User.Role.USER, userResponse.getUserRole());
                    Assertions.assertEquals("user1", userResponse.getUserName());
                    Assertions.assertEquals("user1@email.com", userResponse.getUserEmail());
                }
        );
    }

    /**
     * 이메일이 틀렸을 때
     * 비밀번호가 틀렸을 때
     * case 나누어서 만들기
     */
    @Test
    @Comment("로그인_실패")
    void loginUser_fail() {

        UserLoginRequest loginRequest = new UserLoginRequest(
                "",
                "user12345?"
        );

        Mockito.when(userRepository.findUserResponseByUserEmail(Mockito.anyString()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.loginUser(loginRequest);
        });

        Mockito.verify(userRepository, Mockito.times(1)).findUserResponseByUserEmail(Mockito.anyString());
    }
}