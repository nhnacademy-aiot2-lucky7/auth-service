package com.nhnacademy.auth.util;

import com.nhnacademy.common.exception.AesCryptoException;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class AESUtilTest {

    @Autowired
    AESUtil aesUtil;

    @Test
    @DisplayName("암호화/복호화 테스트")
    void testEncryptAndDecrypt() {
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
    void testDifferentIV() {
        String text = "test@nhnacademy.com";

        String encrypted1 = aesUtil.encrypt(text);
        String encrypted2 = aesUtil.encrypt(text);

        Assertions.assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    @DisplayName("비밀키가 32바이트가 아닐 때 예외 발생")
    void testInvalidKey() {
        Dotenv dotenv = mock(Dotenv.class);

        when(dotenv.get("AES_SECRET")).thenReturn("short-key");

        Assertions.assertThrows(AesCryptoException.class,
                () -> new AESUtil(dotenv)
        );
    }

    @Test
    @DisplayName("복호화 과정에서 예외 발생")
    void testDecryptInvalidText() {
        Dotenv dotenv = mock(Dotenv.class);

        when(dotenv.get("AES_SECRET")).thenReturn("11111111111111111111111111111111");

        aesUtil = new AESUtil(dotenv);
        String invalidEncrypted = "not-encrypt-text";

        Assertions.assertThrows(AesCryptoException.class,
                () -> aesUtil.decrypt(invalidEncrypted)
        );
    }
}