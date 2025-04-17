package com.nhnacademy.token.repository;

import com.nhnacademy.token.domain.RefreshToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final String testUserId = "test-user";
    private final String testToken = "test-refresh-token";

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteById(testUserId);
    }

    @Test
    @DisplayName("RefreshToken 저장")
    void saveTest() {
        RefreshToken refreshToken = new RefreshToken(testUserId, testToken);
        refreshTokenRepository.save(refreshToken);

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(testUserId);
        Assertions.assertTrue(optionalRefreshToken.isPresent());
        Assertions.assertEquals("test-refresh-token", optionalRefreshToken.get().getToken());
    }

    @Test
    @DisplayName("RefreshToken 조회")
    void findByIdTest() {
        RefreshToken refreshToken = new RefreshToken(testUserId, testToken);
        refreshTokenRepository.save(refreshToken);

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(testUserId);
        Assertions.assertTrue(optionalRefreshToken.isPresent());
        Assertions.assertEquals("test-user", optionalRefreshToken.get().getUserId());
    }

    @Test
    @DisplayName("RefreshToken 삭제")
    void deleteTest() {
        RefreshToken refreshToken = new RefreshToken(testUserId, testToken);
        refreshTokenRepository.save(refreshToken);

        refreshTokenRepository.deleteById(testUserId);
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(testUserId);
        Assertions.assertFalse(optionalRefreshToken.isPresent());
    }

    @Test
    @DisplayName("RefreshToken 수정")
    void updateTest() {
        RefreshToken original = new RefreshToken(testUserId, "original-token");
        refreshTokenRepository.save(original);

        RefreshToken updated = new RefreshToken(testUserId, "updated-token");
        refreshTokenRepository.save(updated);

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(testUserId);
        Assertions.assertTrue(optionalRefreshToken.isPresent());
        Assertions.assertEquals("updated-token", optionalRefreshToken.get().getToken());
    }
}