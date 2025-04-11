package com.nhnacademy.user.repository;

import com.nhnacademy.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@Slf4j
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void existsByUserEmail() {

        User user = User.ofNewUser(
                "user",
                "user@email.com",
                "userPassword"
        );
        testEntityManager.persist(user);

        User dbUser = testEntityManager.find(User.class, user.getUserNo());

        Assertions.assertNotNull(dbUser);

        Assertions.assertEquals("user@email.com", dbUser.getUserEmail());
    }
}