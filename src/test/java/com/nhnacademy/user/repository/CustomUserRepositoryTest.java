package com.nhnacademy.user.repository;

import com.nhnacademy.common.exception.NotFoundException;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserResponse;
import jakarta.persistence.EntityManager;
import org.hibernate.annotations.Comment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class CustomUserRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    User settingUser(){
        User user = User.ofNewUser(
                "user",
                "user@email.com",
                "userPassword"
        );

        return userRepository.save(user);
    }

    @Test
    @Comment("userNo로 userResponse 객체 반환")
    void findUserResponseByUserNo() {
        User user = settingUser();
        entityManager.clear();
        Optional<UserResponse> response = Optional.ofNullable(userRepository.findUserResponseByUserNo(user.getUserNo()).orElseThrow(
                () -> new NotFoundException("findUserResponseByUserNo fail")
        ));

        Assertions.assertNotNull(response);

        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals(User.Role.USER, response.get().getUserRole());
                    Assertions.assertEquals("user@email.com", response.get().getUserEmail());
                    Assertions.assertEquals("user", response.get().getUserName());
                    Assertions.assertEquals(user.getUserNo(), response.get().getUserNo());
                }
        );
    }

    @Test
    @Comment("userEmail로 userResponse 객체 반환")
    void findUserResponseByUserEmail() {

        User user = settingUser();
        entityManager.clear();
        Optional<UserResponse> response = Optional.ofNullable(userRepository.findUserResponseByUserEmail(user.getUserEmail()).orElseThrow(
                () -> new NotFoundException("findUserResponseByUserEmail fail")
        ));

        Assertions.assertNotNull(response);

        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals(User.Role.USER, response.get().getUserRole());
                    Assertions.assertEquals("user@email.com", response.get().getUserEmail());
                    Assertions.assertEquals("user", response.get().getUserName());
                    Assertions.assertEquals(user.getUserNo(), response.get().getUserNo());
                }
        );
    }
}