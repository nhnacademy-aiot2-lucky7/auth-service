package com.nhnacademy.auth.util;

import com.nhnacademy.common.exception.AesCryptoException;
import io.github.cdimascio.dotenv.Dotenv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * AESUtil 클래스는 AES 알고리즘을 사용하여 데이터를 암호화하고 복호화하는 유틸리티 클래스입니다.
 * <p>
 * 이 클래스는 GCM 모드에서 AES 알고리즘을 사용하여 안전한 암호화 및 복호화를 제공합니다.
 * GCM 모드는 인증된 암호화를 제공하며, IV(Initialization Vector)와 인증 태그를 포함한 구조로 안전성을 보장합니다.
 * </p>
 */
@Slf4j
@Component
public class AESUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // 96비트
    private static final int TAG_BIT_LENGTH = 128; // 인증 태그 16바이트
    private static final String AES_SECRET = "AES_SECRET";

    private final SecretKeySpec keySpec;

    /**
     * 생성자에서 AES 키를 초기화합니다.
     *
     *
     * @throws AesCryptoException 비밀키가 256비트(32바이트)가 아닌 경우 예외를 던짐
     */
    public AESUtil() {
        log.info("AESUtil 생성자 진입");
        String secretKey = null;

        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            secretKey = dotenv.get(AES_SECRET);
            log.info("env success");
        } catch (Exception ignored) {
            log.info(".env파일에서 키 추출 실패. properties파일로 넘어감.");
        }

        if (secretKey == null || secretKey.trim().isBlank()) {
            secretKey = System.getProperty("aes.secret");
            log.info("System.getProperty(AES_SECRET): {}", secretKey);
            if (secretKey == null) {
                secretKey = System.getenv(AES_SECRET);
                log.info("System.getenv(AES_SECRET): {}", secretKey);
            }
        }

        if (secretKey == null || secretKey.isBlank()) {
            throw new AesCryptoException("AES_SECRET가 설정되지 않았습니다.");
        }

        this.keySpec = getKeySpec(secretKey);
    }

    /**
     * 주어진 평문을 AES 알고리즘을 사용하여 암호화합니다.
     * <p>
     * 암호화된 텍스트는 Base64로 인코딩되어 반환됩니다.
     * </p>
     *
     * @param plainText 암호화할 평문
     * @return 암호화된 텍스트 (Base64 인코딩된 문자열)
     * @throws AesCryptoException 암호화 과정에서 발생할 수 있는 예외
     */

    public String encrypt(String plainText) {
        try {
            byte[] iv = generateRandomIV();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            byte[] encryptedWithIv = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, encryptedWithIv, IV_SIZE, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new AesCryptoException("암호화 과정에서 에러가 발생하였습니다.");
        }
    }

    /**
     * 주어진 암호문을 AES 알고리즘을 사용하여 복호화합니다.
     * <p>
     * 입력된 암호문은 Base64로 디코딩된 후 복호화됩니다.
     * </p>
     *
     * @param encryptedText 암호화된 텍스트 (Base64 인코딩된 문자열)
     * @return 복호화된 평문
     * @throws AesCryptoException 복호화 과정에서 발생할 수 있는 예외
     */

    public String decrypt(String encryptedText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_SIZE];
            byte[] encrypted = new byte[decoded.length - IV_SIZE];

            System.arraycopy(decoded, 0, iv, 0, IV_SIZE);
            System.arraycopy(decoded, IV_SIZE, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted);
        } catch (Exception e) {
            throw new AesCryptoException("복호화 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀키를 SecretKeySpec 객체로 변환합니다.
     * <p>
     * AES 암호화는 256비트(32바이트) 비밀키만 지원하며, 다른 길이의 키는 예외를 발생시킵니다.
     * </p>
     *
     * @param secretKey 비밀키
     * @return SecretKeySpec 객체
     * @throws AesCryptoException 비밀키 길이가 256비트(32바이트)가 아닌 경우 예외 발생
     */

    private SecretKeySpec getKeySpec(String secretKey) {
        byte[] keyBytes = secretKey.getBytes();
        if (keyBytes.length != 32) {
            throw new AesCryptoException("AES 키는 256비트(32바이트)여야 합니다. 현재 길이: " + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 랜덤 IV(Initialization Vector)를 생성합니다.
     * <p>
     * IV는 AES 암호화에서 보안을 강화하기 위해 사용되며, 매번 다른 암호문을 생성하기 위해 필요합니다.
     * </p>
     *
     * @return 생성된 IV (96비트)
     */

    private byte[] generateRandomIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}