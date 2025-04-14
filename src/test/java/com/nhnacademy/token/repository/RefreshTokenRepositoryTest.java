package com.nhnacademy.token.repository;

import com.nhnacademy.auth.provider.JwtProvider;
import com.nhnacademy.auth.util.AESUtil;
import com.nhnacademy.token.domain.RefreshToken;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

import java.util.Optional;


@DataRedisTest
@Slf4j
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setup() {
        String aesSecretKey = "6xm70v3kdyoft71r01bevklfq10zxs0d";
        String jwtSecretKey = "This-Secret-Key-Is-Test-Secret-Key";


        AESUtil aesUtil = new AESUtil(aesSecretKey);
        jwtProvider =  new JwtProvider(jwtSecretKey, aesUtil);
    }

    @AfterEach
    void teardown() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("refreshToken 발급 후 저장 및 조회")
    void saveRefreshTokenAndFindById() {
        String userId = "test@nhnacademy.com";
        String token = jwtProvider.createRefreshToken(userId);

        RefreshToken refreshToken = new RefreshToken(userId, token);

        refreshTokenRepository.save(refreshToken);
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(userId);

        Assertions.assertTrue(optionalRefreshToken.isPresent());
        Assertions.assertEquals(optionalRefreshToken.get().getRefreshToken(), token);
        Assertions.assertEquals("test@nhnacademy.com", optionalRefreshToken.get().getUserId());
        Assertions.assertEquals("test@nhnacademy.com", jwtProvider.getUserIdFromToken(optionalRefreshToken.get().getRefreshToken()));
    }
}