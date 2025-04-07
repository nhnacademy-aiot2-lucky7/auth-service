package com.nhnacademy.user.repository;

import com.nhnacademy.user.domain.User;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Test
    void existsByUserEmail() {

        User user = User.ofNewUser(
                "user",
                "user@email.com",
                "userPassword"
        );
        entityManager.persist(user);

        User dbUser = entityManager.find(User.class, user.getUserNo());

        Assertions.assertNotNull(dbUser);

        Assertions.assertEquals("user@email.com", dbUser.getUserEmail());
    }
}