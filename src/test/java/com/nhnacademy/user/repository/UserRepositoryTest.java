package com.nhnacademy.user.repository;

import com.nhnacademy.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * {@link UserRepository}의 기능 중 일부를 검증하는 테스트 클래스입니다.
 */
@Slf4j
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    /**
     * 사용자의 이메일이 존재하는지 확인하는 테스트
     *
     * 실제 데이터베이스에 사용자 정보를 저장하고,
     * 이메일 기준으로 조회되는지 확인합니다.
     *
     */
    @Test
    void existsByUserEmail() {
        // given: 새로운 사용자 생성 및 저장
        User user = User.ofNewUser(
                "user",
                "user@email.com",
                "userPassword"
        );
        testEntityManager.persist(user);

        // when: 저장된 사용자 조회
        User dbUser = testEntityManager.find(User.class, user.getUserNo());

        // then: 저장 및 조회 결과 확인
        Assertions.assertNotNull(dbUser);
        Assertions.assertEquals("user@email.com", dbUser.getUserEmail());
    }
}