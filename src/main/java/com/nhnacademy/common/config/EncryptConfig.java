package com.nhnacademy.common.config;

import com.common.AESUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AES 암호화 유틸 {@link AESUtil}을 Spring Bean으로 등록하는 설정 클래스입니다.
 * <p>
 * application.properties 또는 환경 변수에서 주입된 AES 키를 이용해
 * AESUtil에 키를 설정하고 단일 Bean으로 제공합니다.
 */
@Configuration
public class EncryptConfig {

    @Value("${aes.secret.key}")
    private String secretKey;

    /**
     * AESUtil Bean 등록
     *
     * @return AESUtil 인스턴스 (secretKey 설정됨)
     */
    @Bean
    public AESUtil aesUtil() {
        AESUtil aesUtil = new AESUtil();
        aesUtil.setKey(secretKey);
        return aesUtil;
    }
}
