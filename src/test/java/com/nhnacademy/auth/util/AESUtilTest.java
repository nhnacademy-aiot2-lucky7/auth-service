package com.nhnacademy.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
class AESUtilTest {
    private AESUtil aesUtil;
    private final String secretKey = "cv0hcgi832wdbft1vh7v0z5x1v814xfh";

    @BeforeEach
    void setup() {
        aesUtil = new AESUtil(secretKey);
    }

    @Test
    @DisplayName("암호화/복호화 테스트")
    void testEncryptAndDecrypt() throws Exception {
        String originalText = "test@nhnacademy.com";

        String encrypted = aesUtil.encrypt(originalText);
        Assertions.assertNotNull(encrypted);
        log.info("암호화된 텍스트: {}", encrypted);

        String decrypted = aesUtil.decrypt(encrypted);
        Assertions.assertNotNull(decrypted);
        Assertions.assertEquals("test@nhnacademy.com", decrypted);
        log.info("복호화된 텍스트: {}", decrypted);
    }

    @Test
    @DisplayName("IV 랜덤 확인")
    void testDifferentIV() throws Exception {
        String text = "test@nhnacademy.com";

        String encrypted1 = aesUtil.encrypt(text);
        String encrypted2 = aesUtil.encrypt(text);

        Assertions.assertNotEquals(encrypted1, encrypted2);
    }
}