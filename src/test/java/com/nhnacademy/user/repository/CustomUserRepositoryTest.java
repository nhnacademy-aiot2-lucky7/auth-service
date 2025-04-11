package com.nhnacademy.user.repository;

import com.nhnacademy.common.exception.NotFoundException;
import com.nhnacademy.user.domain.User;
import com.nhnacademy.user.dto.UserResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


/**
 * 사용자 관련 커스텀 레포지토리 테스트 클래스
 */
@DataJpaTest
@ActiveProfiles("test")
class CustomUserRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    /**
     * 테스트를 위한 사용자 등록 메서드
     *
     * @return 저장된 User 엔티티
     */
    User settingUser(){
        User user = User.ofNewUser(
                "user",
                "user@email.com",
                "userPassword"
        );

        return userRepository.save(user);
    }

    /**
     * userNo를 기반으로 UserResponse 객체를 반환하는 테스트
     * <p>정상적으로 조회되는지 확인</p>
     */
    @Test
    @DisplayName("userNo로 userResponse 객체 반환")
    void findUserResponseByUserNo() {
        User user = settingUser();
        entityManager.clear();
        UserResponse response = userRepository.findUserResponseByUserNo(user.getUserNo())
                .orElseThrow(() -> new NotFoundException("findUserResponseByUserNo fail"));


        Assertions.assertNotNull(response);

        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals(User.Role.USER, response.getUserRole());
                    Assertions.assertEquals("user@email.com", response.getUserEmail());
                    Assertions.assertEquals("user", response.getUserName());
                    Assertions.assertEquals(user.getUserNo(), response.getUserNo());
                }
        );
    }

    /**
     * userEmail을 기반으로 UserResponse 객체를 반환하는 테스트
     * <p>정상적으로 조회되는지 확인</p>
     */
    @Test
    @DisplayName("userEmail로 userResponse 객체 반환")
    void findUserResponseByUserEmail() {

        User user = settingUser();
        entityManager.clear();
        UserResponse response = userRepository.findUserResponseByUserEmail(user.getUserEmail())
                .orElseThrow(() -> new NotFoundException("findUserResponseByUserEmail fail"));

        Assertions.assertNotNull(response);

        Assertions.assertAll(
                ()->{
                    Assertions.assertEquals(User.Role.USER, response.getUserRole());
                    Assertions.assertEquals("user@email.com", response.getUserEmail());
                    Assertions.assertEquals("user", response.getUserName());
                    Assertions.assertEquals(user.getUserNo(), response.getUserNo());
                }
        );
    }
}