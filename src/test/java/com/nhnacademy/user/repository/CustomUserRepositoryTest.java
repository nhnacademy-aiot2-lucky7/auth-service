package com.nhnacademy.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DataJpaTest
class CustomUserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    void findUserResponseByUserNo() {

    }

    @Test
    void findUserResponseByUserEmail() {
    }
}