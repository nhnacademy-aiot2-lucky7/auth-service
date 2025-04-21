package com.nhnacademy.auth.util;

import com.nhnacademy.common.exception.AesCryptoException;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class AESUtilTest {

    @Autowired
    AESUtil aesUtil;

    @Test
    @DisplayName("Dotenv에서 AES 키를 정상적으로 읽어오는 경우")
    void constructor_withDotenvKey() {
        Dotenv dotenv = mock(Dotenv.class);
        Environment env = mock(Environment.class);

        when(dotenv.get("AES_SECRET")).thenReturn("this-is-my-aes-Secret-Key-123123");

        AESUtil aesUtil = new AESUtil(dotenv, env);

        Assertions.assertNotNull(aesUtil);
    }

    @Test
    @DisplayName("Dotenv가 null이고 Environment에서 AES 키를 읽어오는 경우")
    void constructor_withEnvironmentKey() {
        Dotenv dotenv = mock(Dotenv.class);
        Environment env = mock(Environment.class);

        when(dotenv.get("AES_SECRET")).thenReturn(null);
        when(env.getProperty("aes.secret")).thenReturn("this-is-my-aes-Secret-Key-123123");

        AESUtil aesUtil = new AESUtil(dotenv, env);

        Assertions.assertNotNull(aesUtil);
    }

    @Test
    @DisplayName("Dotenv와 Environment 모두 비어있을 경우 예외 발생")
    void constructor_throwsExceptionWhenKeyIsMissing() {
        Dotenv dotenv = mock(Dotenv.class);
        Environment env = mock(Environment.class);

        when(dotenv.get("AES_SECRET")).thenReturn(null);
        when(env.getProperty("aes.secret")).thenReturn(null);

        Exception exception = Assertions.assertThrows(AesCryptoException.class, () -> {
            new AESUtil(dotenv, env);
        });

        Assertions.assertEquals("AES_SECRET가 설정되지 않았습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("Dotenv 키가 빈 문자열이고 Environment에서 정상적으로 읽는 경우")
    void constructor_blankDotenvKey_useEnvInstead() {
        Dotenv dotenv = mock(Dotenv.class);
        Environment env = mock(Environment.class);

        when(dotenv.get("AES_SECRET")).thenReturn("   ");
        when(env.getProperty("aes.secret")).thenReturn("this-is-my-aes-Secret-Key-123123");

        aesUtil = new AESUtil(dotenv, env);

        Assertions.assertNotNull(aesUtil);
    }

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
    @DisplayName("복호화 중 예외 발생(AesCryptoException)")
    void testDecryptInvalidText() {
        String invalidEncrypted = "not-encrypt-text";

        Assertions.assertThrows(AesCryptoException.class,
                () -> aesUtil.decrypt(invalidEncrypted)
        );
    }

    @Test
    @DisplayName("32바이트가 아닌 secretKey 입력 시 AesCryptoException 발생")
    void constructor_throwsException_whenKeyIsNot32Bytes() {
        // Given
        Dotenv dotenv = mock(Dotenv.class);
        Environment env = mock(Environment.class);

        // 32바이트가 아닌 짧은 키 (예: 5바이트)
        when(dotenv.get("AES_SECRET")).thenReturn("short");
        when(env.getProperty("aes.secret")).thenReturn(null); // fallback도 null

        // When & Then
        AesCryptoException exception = Assertions.assertThrows(AesCryptoException.class,
                () -> new AESUtil(dotenv, env)
        );

        Assertions.assertTrue(exception.getMessage().contains("현재 길이: 5"));
    }

    @Test
    @DisplayName("IV 랜덤 확인")
    void testDifferentIV() {
        String text = "test@nhnacademy.com";

        String encrypted1 = aesUtil.encrypt(text);
        String encrypted2 = aesUtil.encrypt(text);

        Assertions.assertNotEquals(encrypted1, encrypted2);
    }
}